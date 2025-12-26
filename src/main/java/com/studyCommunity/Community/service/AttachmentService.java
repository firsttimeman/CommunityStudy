package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.AttachmentDownloadStreamResult;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.AttachmentDeleteException;
import com.studyCommunity.Community.exception.AttachmentUploadException;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3Uploader s3Uploader;

    public List<Long> upload(List<MultipartFile> files, String userId) {

        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<Long> attachmentIds = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                String s3Key = s3Uploader.upload(file);
                uploadedKeys.add(s3Key);

                Attachment attachment = Attachment.builder()
                        .attachmentStatus(AttachmentStatus.TEMP)
                        .s3Key(s3Key)
                        .originalFileName(file.getOriginalFilename())
                        .fileSize((int) file.getSize())
                        .userId(userId)
                        .build();

                attachmentRepository.save(attachment);
                attachmentIds.add(attachment.getAttachmentId());
            }

            return attachmentIds;

        } catch (Exception e) {
            for (String key : uploadedKeys) {
                try {
                    s3Uploader.delete(key);
                } catch (Exception ignore) {
                    log.error("S3 rollback delete failed. key={}", key, ignore);
                }
            }
            throw new AttachmentUploadException("Attachment upload failed", e);
        }
    }

    @Transactional
    public void attachToPost(List<Long> attachmentIds, Post post, String userId) {
        if(attachmentIds == null || attachmentIds.isEmpty()) return;

        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);

        if(attachments.size() != attachmentIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 첨부파일 ID가 포함되어 있습니다.");
        }

        for (Attachment attachment : attachments) {
            if(!userId.equals(attachment.getUserId())) {
                throw new IllegalArgumentException("첨부파일 업로더가 아닙니다.");
            }

            if(attachment.getAttachmentStatus() != AttachmentStatus.TEMP) {
                throw new IllegalStateException("이미 사용된 첨부파일입니다. attachmentId=" + attachment.getAttachmentId());
            }

            attachment.attachTo(post);
        }

    }



    @Transactional
    public void deleteAllPost(Post post) {
        List<Attachment> attachments = attachmentRepository.findAllByPost(post);

        for (Attachment attachment : attachments) {
            try {
                s3Uploader.delete(attachment.getS3Key());
            } catch (Exception e) {
                throw new AttachmentDeleteException(
                        "S3 delete failed: " + attachment.getS3Key(), e
                );
            }
        }

        attachmentRepository.deleteAll(attachments);
    }

    @Transactional
    public void deleteAttachmentByIds(List<Long> attachmentIds, String userId) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;

        List<Attachment> attachments = attachmentRepository.findAllById(attachmentIds);

        if (attachments.size() != attachmentIds.size()) {
            throw new IllegalArgumentException("존재하지 않는 첨부파일 ID가 포함되어 있습니다.");
        }

        for (Attachment a : attachments) {
            Post post = a.getPost();

            if (post == null) {
                if (!userId.equals(a.getUserId())) {
                    throw new IllegalArgumentException("첨부파일 업로더만 삭제할 수 있습니다.");
                }
            } else {
                if (!userId.equals(post.getUserId())) {
                    throw new IllegalArgumentException("게시글 작성자만 첨부파일을 삭제할 수 있습니다.");
                }
            }
        }

        for (Attachment a : attachments) {
            try {
                s3Uploader.delete(a.getS3Key());
            } catch (Exception e) {
                throw new AttachmentDeleteException("S3 delete failed. key=" + a.getS3Key(), e);
            }
        }

        attachmentRepository.deleteAllInBatch(attachments);
    }

    @Transactional
    public AttachmentDownloadStreamResult download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("첨부파일이 존재하지 않습니다."));

        if (attachment.getAttachmentStatus() != AttachmentStatus.ATTACHED) {
            throw new IllegalStateException("게시글에 첨부되지 않은 파일입니다.");
        }


        ResponseInputStream<GetObjectResponse> s3Stream =
                s3Uploader.downloadStream(attachment.getS3Key());

        GetObjectResponse meta = s3Stream.response();

        return new AttachmentDownloadStreamResult(
                attachment.getOriginalFileName(),
                meta.contentType(),
                meta.contentLength() != null ? meta.contentLength() : -1L,
               new InputStreamResource(s3Stream)
        );
    }
}


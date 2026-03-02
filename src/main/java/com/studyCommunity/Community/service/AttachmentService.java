package com.studyCommunity.Community.service;

import com.studyCommunity.Community.dto.AttachmentDownloadStreamResult;
import com.studyCommunity.Community.dto.UploadResult;
import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.*;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.repository.PostRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3Uploader s3Uploader;
    private final PostRepository postRepository;

    @Transactional
    public List<Long> upload(List<MultipartFile> files, String userId) {
        if (files == null || files.isEmpty()) return List.of();

        List<Long> attachmentIds = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();

        try {
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;

                UploadResult result = uploadOne(file, userId);
                attachmentIds.add(result.attachmentId());
                uploadedKeys.add(result.s3Key());
            }
            return attachmentIds;

        } catch (Exception e) {
            rollbackS3Uploads(uploadedKeys, e);
            throw new AttachmentUploadException("Attachment upload failed", e);
        }
    }

    private UploadResult uploadOne(MultipartFile file, String userId) {
        String s3Key = s3Uploader.upload(file);

        Attachment attachment = Attachment.builder()
                .attachmentStatus(AttachmentStatus.TEMP)
                .s3Key(s3Key)
                .originalFileName(file.getOriginalFilename())
                .fileSize((int) file.getSize())
                .userId(userId)
                .expireAt(LocalDateTime.now().plusHours(1))
                .build();

        attachmentRepository.save(attachment);
        return new UploadResult(attachment.getAttachmentId(), s3Key);
    }

    private void rollbackS3Uploads(List<String> uploadedKeys, Exception cause) {
        if (uploadedKeys.isEmpty()) return;

        log.warn("Upload failed. rollback S3 objects best-effort. count={}, cause={}",
                uploadedKeys.size(), cause.toString());

        for (String key : uploadedKeys) {
            try {
                s3Uploader.delete(key);
            } catch (Exception deleteEx) {
                log.error("S3 rollback delete failed. key={}", key, deleteEx);
            }
        }
    }

    @Transactional
    public void attachToPost(List<Long> attachmentIds, Post post, String userId) {
        if (attachmentIds == null || attachmentIds.isEmpty()) return;

        int updated = attachmentRepository.attachTempToPostAtomically(post, attachmentIds, userId);


        if (updated != attachmentIds.size()) {
            throw new ForbiddenException("첨부파일이 이미 사용되었거나 권한이 없거나 유효하지 않은 ID가 포함되어 있습니다.");
        }

        postRepository.increaseAttachmentCount(post.getPostId(), attachmentIds.size());
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
    public void deleteAttachmentByIds(Long postId , List<Long> attachmentIds, String userId) {
        if (attachmentIds == null || attachmentIds.isEmpty()) {
            throw new BadRequestException("첨부파일 ID 목록은 비어 있을 수 없습니다.");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("POST NOT FOUND"));


        if (!userId.equals(post.getUserId())) {
            throw new ForbiddenException("게시글 작성자만 첨부파일을 삭제할 수 있습니다.");
        }

        List<Attachment> attachments =
                attachmentRepository.findAllByAttachmentIdInAndPost_PostId(attachmentIds, postId);

        if (attachments.size() != attachmentIds.size()) {
            throw new NotFoundException("존재하지 않는 첨부파일 ID가 포함되어 있습니다.");
        }

        for (Attachment a : attachments) {
            if (a.getAttachmentStatus() != AttachmentStatus.ATTACHED) {
                throw new BadRequestException("게시글에 첨부된 파일만 삭제할 수 있습니다.");
            }
        }

        for (Attachment a : attachments) {
            try {
                s3Uploader.delete(a.getS3Key());
            } catch (Exception e) {
                throw new AttachmentDeleteException("S3 delete failed. key="  + a.getS3Key(), e);
            }
        }

        attachmentRepository.deleteAllInBatch(attachments);
        postRepository.decreaseAttachmentCount(postId, attachments.size());

    }

    @Transactional
    public AttachmentDownloadStreamResult download(Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new NotFoundException("첨부파일이 존재하지 않습니다."));

        if (attachment.getAttachmentStatus() != AttachmentStatus.ATTACHED) {
            throw new ForbiddenException("게시글에 첨부되지 않은 파일입니다.");
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


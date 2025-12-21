package com.studyCommunity.Community.service;

import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.entity.Post;
import com.studyCommunity.Community.exception.AttachmentDeleteException;
import com.studyCommunity.Community.exception.AttachmentUploadException;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
}


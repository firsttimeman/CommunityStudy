package com.studyCommunity.Community.service;

import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentUploadService {

    private final AttachmentRepository attachmentRepository;
    private final S3Uploader s3Uploader;

    public List<Long> upload(List<MultipartFile> files, String userId) {

        if(files == null || files.isEmpty()) {
            return List.of();
        }

        List<Long> attachmentIds = new ArrayList<>();

        for (MultipartFile file : files) {

            String s3Key = s3Uploader.upload(file);

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
    }

}

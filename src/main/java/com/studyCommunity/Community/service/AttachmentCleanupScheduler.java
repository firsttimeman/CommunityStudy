package com.studyCommunity.Community.service;

import com.studyCommunity.Community.entity.Attachment;
import com.studyCommunity.Community.infra.S3Uploader;
import com.studyCommunity.Community.repository.AttachmentRepository;
import com.studyCommunity.Community.type.AttachmentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AttachmentCleanupScheduler {

    private final AttachmentRepository attachmentRepository;
    private final S3Uploader s3Uploader;

    @SchedulerLock(
            name = "AttachmentCleanupScheduler.cleanUpExpiredTempAttachments",
            lockAtLeastFor = "PT10S",
            lockAtMostFor = "PT3M"
    )
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    @Transactional
    public void cleanUpExpiredTempAttachments() {
        List<Attachment> targets =
                attachmentRepository.findExpiredTempAttachments(
                        AttachmentStatus.TEMP,
                        LocalDateTime.now(),
                        PageRequest.of(0, 100)
                );

        if (targets.isEmpty()) return;

        List<Attachment> success = new ArrayList<>();
        List<String> failedKeys = new ArrayList<>();

        for (Attachment a : targets) {
            try {
                s3Uploader.delete(a.getS3Key());
                success.add(a);
            } catch (Exception e) {
                failedKeys.add(a.getS3Key());
                log.error("TEMP 삭제 시도 실패 attachmentIds={}, key{}", a.getAttachmentId(), a.getS3Key());
            }
        }

        if (!success.isEmpty()) {
            attachmentRepository.deleteAllInBatch(success);

        }

        log.info("TEMP 청소 완료. target={}, deleted={}, failed={}",
                targets.size(), success.size(), failedKeys.size());
    }


}

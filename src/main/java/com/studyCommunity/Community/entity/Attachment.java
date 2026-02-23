package com.studyCommunity.Community.entity;

import com.studyCommunity.Community.exception.BadRequestException;
import com.studyCommunity.Community.type.AttachmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "attachment",
        indexes = {
            @Index(name = "idx_attachment_postid", columnList = "post_id"),
                @Index(name = "idx_attachment_status_expireat", columnList = "attachment_status, expire_at")
        }
)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attachmentId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private int fileSize;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AttachmentStatus attachmentStatus;

    @Column(nullable = true)
    private LocalDateTime expireAt;

    public void attachTo(Post post) {
        if (post == null) {
            throw new BadRequestException("게시글은 null일 수 없습니다.");
        }

        if (this.attachmentStatus != AttachmentStatus.TEMP) {
            throw new BadRequestException("이미 게시글에 첨부된 파일입니다.");
        }

        this.post = post;
        this.attachmentStatus = AttachmentStatus.ATTACHED;
        this.expireAt = null;
    }

}

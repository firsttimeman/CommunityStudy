package com.studyCommunity.Community.entity;

import com.studyCommunity.Community.type.AttachmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends BaseEntity{

    @Id @GeneratedValue
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

    public void attachedTo(Post post) {
        this.post = post;
        this.attachmentStatus = AttachmentStatus.ATTACHED;
    }

}

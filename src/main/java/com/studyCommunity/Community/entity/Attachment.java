package com.studyCommunity.Community.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
public class Attachment extends BaseEntity{

    @Id @GeneratedValue
    private Long attachmentId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private Integer fileSize;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String s3Key;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "post_id")
    private Post post;


}

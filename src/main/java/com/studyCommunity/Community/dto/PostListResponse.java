package com.studyCommunity.Community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {
    private Long postId;
    private String title;
    private String userId;
    private LocalDateTime createdAt;
    private long commentCount;
    private long attachmentCount;
}
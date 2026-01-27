package com.studyCommunity.Community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailResponse {
    private Long postId;
    private String title;
    private String content;
    private String userId;
    private LocalDateTime createdAt;

    private List<AttachmentResponse> attachments;
    private List<CommentResponse> comments;
}

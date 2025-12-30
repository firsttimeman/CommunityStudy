package com.studyCommunity.Community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateRequest {
    @NotBlank(message = "댓글 내용은 필수입니다.")
    @Size(max = 300, message = "댓글은 300자 이하여야 합니다.")
    private String content;
}

package com.studyCommunity.Community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long attachmentId;
    private String originalFileName;
    private String s3Key;
}

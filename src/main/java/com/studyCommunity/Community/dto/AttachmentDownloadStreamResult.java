package com.studyCommunity.Community.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.io.Resource;


@Getter
@AllArgsConstructor
public class AttachmentDownloadStreamResult {
    private String originalFileName;
    private String contentType;
    private long contentLength;
    private Resource resource;
}

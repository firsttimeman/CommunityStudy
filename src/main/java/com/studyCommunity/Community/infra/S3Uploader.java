package com.studyCommunity.Community.infra;

import org.springframework.web.multipart.MultipartFile;

public interface S3Uploader {
    String upload(MultipartFile file);

    void delete(String s3Key);
}

package com.studyCommunity.Community.infra;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public interface S3Uploader {
    String upload(MultipartFile file);

    void delete(String s3Key);

    ResponseInputStream<GetObjectResponse> downloadStream(String s3Key);
}


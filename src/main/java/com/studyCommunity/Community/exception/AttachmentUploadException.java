package com.studyCommunity.Community.exception;

public class AttachmentUploadException extends CustomException {
    public AttachmentUploadException(String message) {
        super(500, "ATTACHMENT_UPLOAD_FAILED", message);
    }

    public AttachmentUploadException(String message, Throwable cause) {
        super(500, "ATTACHMENT_UPLOAD_FAILED", message, cause);
    }
}

package com.studyCommunity.Community.exception;

public class AttachmentDeleteException extends CustomException {
    public AttachmentDeleteException(String message) {
        super(500, "ATTACHMENT_DELETE_FAILED", message);
    }

    public AttachmentDeleteException(String message, Throwable cause) {
        super(500, "ATTACHMENT_DELETE_FAILED", message, cause);
    }
}

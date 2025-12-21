package com.studyCommunity.Community.exception;

public class AttachmentDeleteException extends RuntimeException{
    public AttachmentDeleteException(String message) {
        super(message);
    }

    public AttachmentDeleteException(String message, Throwable cause) {
        super(message, cause);
    }
}

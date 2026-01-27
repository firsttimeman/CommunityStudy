package com.studyCommunity.Community.exception;

public class BadRequestException extends CustomException {
    public BadRequestException(String message) {
        super(400, "BAD_REQUEST", message);
    }
}

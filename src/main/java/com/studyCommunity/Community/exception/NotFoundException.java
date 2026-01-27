package com.studyCommunity.Community.exception;

public class NotFoundException extends CustomException {
    public NotFoundException(String message) {
        super(404, "NOT_FOUND", message);
    }
}

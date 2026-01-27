package com.studyCommunity.Community.controller;

import org.springframework.http.HttpStatus;

public class BaseResponse<T> {
    private final T result;
    private final String message;
    private final int code;

    private BaseResponse(T result, HttpStatus status) {
        this.result = result;
        this.message = status.getReasonPhrase();
        this.code = status.value();
    }

    public static <T> BaseResponse<T> of(T result, HttpStatus status) {
        return new BaseResponse<>(result, status);
    }

    public static BaseResponse<Void> of(HttpStatus status) {
        return new BaseResponse<>(null, status);
    }
}
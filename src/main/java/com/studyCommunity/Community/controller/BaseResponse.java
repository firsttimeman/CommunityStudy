package com.studyCommunity.Community.controller;

//todo 다시 한번더 code에 대해서 물어보기
public class BaseResponse<T> {
    private final T result;
    private final String message;
    private final int code;

    public BaseResponse(T result, String message, int code) {
        this.result = result;
        this.message = message;
        this.code = code;
    }

    public static <T> BaseResponse<T> ok(T result, int code) {
        return new BaseResponse<>(result, "OK", code);
    }
}

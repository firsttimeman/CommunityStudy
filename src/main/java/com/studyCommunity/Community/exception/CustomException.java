package com.studyCommunity.Community.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final int status;
    private final String code;

    public CustomException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public CustomException(int status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }

//    public CustomException(Throwable cause) {
//        super(cause);
//    }
//
//    //todo 이 예외처리가 어디서 쓰이는지 한번 물어보기
//    public CustomException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
//        super(message, cause, enableSuppression, writableStackTrace);
//    }
}

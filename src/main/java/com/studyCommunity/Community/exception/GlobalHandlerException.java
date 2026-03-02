package com.studyCommunity.Community.exception;

import com.studyCommunity.Community.controller.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalHandlerException {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e, HttpServletRequest request) {
        return ResponseEntity.status(e.getStatus())
                .body(ErrorResponse.of(e.getStatus(),
                        e.getCode(), e.getMessage(),request.getRequestURI()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpServletRequest req
    ) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findAny()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("validation failed");

        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(400, "BAD_REQUEST", message, req.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> lockTimeout(IllegalStateException e, HttpServletRequest req) {
        if (e.getMessage() != null && e.getMessage().startsWith("LOCK_TIMEOUT")) {
            return ResponseEntity.status(TOO_MANY_REQUESTS)
                    .body(ErrorResponse.of(
                            TOO_MANY_REQUESTS.value(),
                            "LOCK_TIMEOUT",
                            "Lock acquisition timeout",
                            req.getRequestURI()
                    ));
        }

        // 그 외 IllegalStateException은 500으로 통일
        log.error("IllegalState {} {}", req.getMethod(), req.getRequestURI(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        INTERNAL_SERVER_ERROR.value(),
                        "INTERNAL_SERVER_ERROR",
                        "Unexpected error",
                        req.getRequestURI()
                ));

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e, HttpServletRequest req) {
        log.error("Unhandled error {} {}", req.getMethod(), req.getRequestURI(), e);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "Unexpected error", req.getRequestURI()));
    }
}

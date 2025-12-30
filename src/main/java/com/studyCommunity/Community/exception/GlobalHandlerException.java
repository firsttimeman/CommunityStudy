package com.studyCommunity.Community.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalHandlerException {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException e, HttpServletRequest req) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(ErrorResponse.of(400, "BAD_REQUEST", e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException e, HttpServletRequest req) {
        return ResponseEntity.status(NOT_FOUND)
                .body(ErrorResponse.of(404, "NOT_FOUND", e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e, HttpServletRequest req) {
        return ResponseEntity.status(FORBIDDEN)
                .body(ErrorResponse.of(403, "FORBIDDEN", e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e, HttpServletRequest req) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(500, "INTERNAL_SERVER_ERROR", "Unexpected error", req.getRequestURI()));
    }

    @ExceptionHandler(AttachmentDeleteException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentDelete(
            AttachmentDeleteException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "ATTACHMENT_DELETE_FAILED",
                        e.getMessage(),
                        req.getRequestURI()
                ));
    }

    @ExceptionHandler(AttachmentUploadException.class)
    public ResponseEntity<ErrorResponse> handleAttachmentUpload(
            AttachmentUploadException e,
            HttpServletRequest req
    ) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(
                        500,
                        "ATTACHMENT_UPLOAD_FAILED",
                        e.getMessage(),
                        req.getRequestURI()
                ));
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
                .body(ErrorResponse.of(400, message, e.getMessage(), req.getRequestURI()));
    }
}

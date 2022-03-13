package com.example.exception.presentation;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class HttpFacingBaseException extends RuntimeException {

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String userFriendlyMessage;

    public HttpFacingBaseException(HttpStatus httpStatus, String userFriendlyMessage) {
        this.httpStatus = httpStatus;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public HttpFacingBaseException(String message, HttpStatus httpStatus, String userFriendlyMessage) {
        super(message);
        this.httpStatus = httpStatus;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public HttpFacingBaseException(String message, Throwable cause, HttpStatus httpStatus,
            String userFriendlyMessage) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public HttpFacingBaseException(Throwable cause, HttpStatus httpStatus, String userFriendlyMessage) {
        super(cause);
        this.httpStatus = httpStatus;
        this.userFriendlyMessage = userFriendlyMessage;
    }

    public HttpFacingBaseException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace, HttpStatus httpStatus, String userFriendlyMessage) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.httpStatus = httpStatus;
        this.userFriendlyMessage = userFriendlyMessage;
    }
}

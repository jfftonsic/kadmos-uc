package com.example.exception.presentation;

import org.springframework.http.HttpStatus;

public class NotEnoughBalanceHttpException extends HttpFacingBaseException {

    public static final HttpStatus HTTP_STATUS = HttpStatus.BAD_REQUEST;
    public static final String USER_FRIENDLY_MESSAGE = "Not enough balance.";

    public NotEnoughBalanceHttpException() {
        super(HTTP_STATUS, USER_FRIENDLY_MESSAGE);
    }

    public NotEnoughBalanceHttpException(String message) {
        super(message, HTTP_STATUS, USER_FRIENDLY_MESSAGE);
    }

    public NotEnoughBalanceHttpException(String message, Throwable cause) {
        super(message, cause, HTTP_STATUS, USER_FRIENDLY_MESSAGE);
    }

    public NotEnoughBalanceHttpException(Throwable cause) {
        super(cause, HTTP_STATUS, USER_FRIENDLY_MESSAGE);
    }

    public NotEnoughBalanceHttpException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, HTTP_STATUS, USER_FRIENDLY_MESSAGE);
    }
}

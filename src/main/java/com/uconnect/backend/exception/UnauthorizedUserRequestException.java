package com.uconnect.backend.exception;

public class UnauthorizedUserRequestException extends RuntimeException {
    public UnauthorizedUserRequestException() {
        super();
    }

    public UnauthorizedUserRequestException(String message) {
        super(message);
    }

    public UnauthorizedUserRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnauthorizedUserRequestException(Throwable cause) {
        super(cause);
    }
}

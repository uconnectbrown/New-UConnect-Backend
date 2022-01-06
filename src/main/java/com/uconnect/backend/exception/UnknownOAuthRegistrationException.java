package com.uconnect.backend.exception;

public class UnknownOAuthRegistrationException extends RuntimeException {
    public UnknownOAuthRegistrationException() {
        super();
    }

    public UnknownOAuthRegistrationException(String message) {
        super(message);
    }

    public UnknownOAuthRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnknownOAuthRegistrationException(Throwable cause) {
        super(cause);
    }
}

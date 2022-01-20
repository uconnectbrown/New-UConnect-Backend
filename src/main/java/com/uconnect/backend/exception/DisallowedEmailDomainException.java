package com.uconnect.backend.exception;

import lombok.Getter;

@Getter
public class DisallowedEmailDomainException extends RuntimeException {
    private String emailAddress;

    public DisallowedEmailDomainException() {
        super();
    }

    public DisallowedEmailDomainException(String message, String emailAddress) {
        super(message);
        this.emailAddress = emailAddress;
    }

    public DisallowedEmailDomainException(String message) {
        super(message);
    }

    public DisallowedEmailDomainException(String message, Throwable cause) {
        super(message, cause);
    }

    public DisallowedEmailDomainException(Throwable cause) {
        super(cause);
    }
}

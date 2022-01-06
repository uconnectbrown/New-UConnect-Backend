package com.uconnect.backend.exception;

import com.uconnect.backend.user.model.UserCreationType;
import lombok.Getter;

@Getter
public class UnmatchedUserCreationTypeException extends RuntimeException {
    private UserCreationType unmatchedCreationType;

    public UnmatchedUserCreationTypeException() {
        super();
    }

    public UnmatchedUserCreationTypeException(UserCreationType unmatchedCreationType) {
        super();
        this.unmatchedCreationType = unmatchedCreationType;
    }

    public UnmatchedUserCreationTypeException(String message) {
        super(message);
    }

    public UnmatchedUserCreationTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnmatchedUserCreationTypeException(Throwable cause) {
        super(cause);
    }
}

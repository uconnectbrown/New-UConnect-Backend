package com.uconnect.backend.exception;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String err) {
        super(err);
    }
}
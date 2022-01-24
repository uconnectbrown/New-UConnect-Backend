package com.uconnect.backend.exception;

public class CourseNotFoundException extends Exception {
    public CourseNotFoundException(String err) {
        super(err);
    }
}
package com.uconnect.backend.exception;

/**
 * Neither an event nor a comment was found by the given parent uuid
 */
public class EventBoardEntityNotFoundException extends Exception {
    public EventBoardEntityNotFoundException() {
        super();
    }

    public EventBoardEntityNotFoundException(String err) {
        super(err);
    }

}
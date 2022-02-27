package com.uconnect.backend.exception;

import lombok.Getter;

@Getter
public class EventBoardCommentNotFoundException extends Exception {
    private String id;

    public EventBoardCommentNotFoundException(String err) {
        super(err);
    }

    public EventBoardCommentNotFoundException(String err, String id) {
        super(err);
        this.id = id;
    }

}
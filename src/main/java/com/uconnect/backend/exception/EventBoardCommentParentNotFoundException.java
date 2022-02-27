package com.uconnect.backend.exception;

import lombok.Getter;

@Getter
/**
 * Neither an event nor a comment was found by the given parent uuid
 */
public class EventBoardCommentParentNotFoundException extends Exception {
    public EventBoardCommentParentNotFoundException() {
        super();
    }

    public EventBoardCommentParentNotFoundException(String err) {
        super(err);
    }

}
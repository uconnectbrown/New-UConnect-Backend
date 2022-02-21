package com.uconnect.backend.exception;

import lombok.Getter;

@Getter
public class EventBoardEventNotFoundException extends Exception {
    private long index = -1;

    public EventBoardEventNotFoundException(String err) {
        super(err);
    }

    public EventBoardEventNotFoundException(String err, long index) {
        super(err);
        this.index = index;
    }

}
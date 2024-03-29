package com.uconnect.backend.helper;

import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.user.model.User;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;

/**
 * Very rudimentary mock data generator for lack of a better one.
 */
public class MockData {
    /**
     * Generate a single User with valid fields.
     *
     * @return A randomly generated User with an {@literal@}brown.edu username.
     */
    public static User generateValidUser() {
        String email = RandomStringUtils.randomAlphanumeric(7) + "@brown.edu";
        return User.builder()
                .username(email)
                .password(RandomStringUtils.randomAlphanumeric(15))
                .firstName(RandomStringUtils.randomAlphanumeric(7))
                .lastName(RandomStringUtils.randomAlphanumeric(7))
                .verified(false)
                .profileCompleted(false)
                .build();
    }

    public static Event generateValidEventBoardEvent(String author) {
        Event event = generateValidEventBoardEvent();
        event.setAuthor(author);

        return event;
    }

    public static Event generateValidEventBoardEvent() {
        return Event.builder()
                .description(RandomStringUtils.randomAlphanumeric(100))
                .timestamp(new Date())
                .startTime(new Date(new Date().getTime() + 5000))
                .title(RandomStringUtils.randomAlphanumeric(25))
                .host(RandomStringUtils.randomAlphanumeric(25))
                .build();
    }

    public static Comment generateValidEventBoardComment(String parentId) {
        Comment comment = generateValidEventBoardComment();
        comment.setParentId(parentId);

        return comment;
    }

    public static Comment generateValidEventBoardComment() {
        return Comment.builder()
                .content(RandomStringUtils.randomAlphanumeric(100))
                .timestamp(new Date())
                .build();
    }
}

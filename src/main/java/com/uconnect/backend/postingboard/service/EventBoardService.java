package com.uconnect.backend.postingboard.service;

import com.uconnect.backend.exception.EventBoardCommentNotFoundException;
import com.uconnect.backend.exception.EventBoardCommentParentNotFoundException;
import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.postingboard.dao.CounterDAO;
import com.uconnect.backend.postingboard.dao.EventBoardDAO;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EventBoardService {

    public static final String ANONYMOUS_AUTHOR = "Anonymous Author";
    public static final String ANONYMOUS_HOST = "Anonymous Host";
    public static final int MAX_SCAN_COUNT = 50;

    private final EventBoardDAO eventBoardDAO;

    private final CounterDAO counterDAO;

    @Autowired
    public EventBoardService(EventBoardDAO eventBoardDAO, CounterDAO counterDAO) {
        this.eventBoardDAO = eventBoardDAO;
        this.counterDAO = counterDAO;
    }

    // -----------
    // ---Event---
    // -----------
    public void newAnonymousEvent(Event event) {
        event.setTimestamp(new Date());
        event.setAnonymous(true);
        event.setAuthor(ANONYMOUS_AUTHOR);
        event.setHost(ANONYMOUS_HOST);
        event.setIndex(-1);

        eventBoardDAO.saveHiddenEvent(event);
    }

    public void newVerifiedEvent(Event event) {
        event.setTimestamp(new Date());
        event.setAnonymous(false);
        long index = counterDAO.incrementEventBoardIndex() - 1;
        event.setIndex(index);

        eventBoardDAO.savePublishedEvent(event);
    }

    public Event getPublishedEventByIndex(long index) throws EventBoardEventNotFoundException {
        Event event = eventBoardDAO.getPublishedEventByIndex(index);
        event.setComments(getPublishedCommentsByParentId(event.getId()));

        return event;
    }

    public GetEventsResponse getLatestPublishedEvents(long startIndex, int eventCount) {
        eventCount = Math.min(eventCount, MAX_SCAN_COUNT);
        eventCount = Math.max(eventCount, 0);
        if (startIndex < 1) {
            startIndex = Long.MAX_VALUE;
        }
        startIndex = Math.min(startIndex, counterDAO.getNextEventBoardIndex() - 1);
        List<Event> acc = new ArrayList<>(eventCount);

        while (startIndex >= 0 && acc.size() < eventCount) {
            try {
                Event result = eventBoardDAO.getPublishedEventByIndex(startIndex);
                result.setComments(getPublishedCommentsByParentId(result.getId()));
                acc.add(result);
            } catch (EventBoardEventNotFoundException ignored) {
            }

            --startIndex;
        }

        long lastQueriedIndex = acc.isEmpty() ? -1 : startIndex + 1;
        return new GetEventsResponse(acc, lastQueriedIndex);
    }

    // -------------
    // ---Comment---
    // -------------
    public void newAnonymousComment(Comment comment) throws EventBoardCommentParentNotFoundException {
        String parentId = comment.getParentId();
        verifyCommentParentExists(parentId);

        comment.setTimestamp(new Date());
        comment.setAnonymous(true);
        comment.setAuthor(ANONYMOUS_AUTHOR);
        // TODO: fix this and save the parent comment as isCommentPresent
        comment.setCommentPresent(true);
        eventBoardDAO.saveHiddenComment(comment);
    }

    public void newVerifiedComment(Comment comment) throws EventBoardCommentParentNotFoundException {
        String parentId = comment.getParentId();
        verifyCommentParentExists(parentId);

        comment.setTimestamp(new Date());
        comment.setAnonymous(false);
        // TODO: fix this and save the parent comment as isCommentPresent
        comment.setCommentPresent(true);
        eventBoardDAO.savePublishedComment(comment);
    }

    public List<Comment> getPublishedCommentsByParentId(String parentId) {
        List<Comment> comments = eventBoardDAO.getPublishedCommentsByParentId(parentId);
        populateChildrenComments(comments);

        return comments;
    }

    private void verifyCommentParentExists(String parentId) throws EventBoardCommentParentNotFoundException {
        try {
            // commenting on an event
            eventBoardDAO.getPublishedEventById(parentId);
        } catch (EventBoardEventNotFoundException e) {
            try {
                // commenting on a comment
                eventBoardDAO.getPublishedCommentById(parentId);
            } catch (EventBoardCommentNotFoundException ce) {
                throw new EventBoardCommentParentNotFoundException();
            }
        }
    }

    private void populateChildrenComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (Comment comment : comments) {
            if (comment != null && comment.isCommentPresent()) {
                List<Comment> children = eventBoardDAO.getPublishedCommentsByParentId(comment.getId());
                comment.setComments(children);

                populateChildrenComments(children);
            }
        }
    }
}

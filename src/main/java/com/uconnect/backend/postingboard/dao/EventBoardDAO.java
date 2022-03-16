package com.uconnect.backend.postingboard.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.EventBoardCommentNotFoundException;
import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Slf4j
public class EventBoardDAO {

    private final DdbAdapter ddbAdapter;

    private final String eventHiddenTableName;

    private final String eventPublishedTableName;

    private final String commentHiddenTableName;

    private final String commentPublishedTableName;

    private final String authorIndexName;

    private final String hostIndexName;

    private final String indexIndexName;

    private final String commentParentIdIndexName;

    @Autowired
    public EventBoardDAO(DdbAdapter ddbAdapter, String eventBoardEventHiddenTableName, String eventBoardEventPublishedTableName,
                         String eventBoardCommentHiddenTableName, String eventBoardCommentPublishedTableName, String eventBoardAuthorIndexName,
                         String eventBoardHostIndexName, String eventBoardIndexIndexName, String eventBoardCommentParentIdIndexName) {
        this.ddbAdapter = ddbAdapter;
        this.eventHiddenTableName = eventBoardEventHiddenTableName;
        this.eventPublishedTableName = eventBoardEventPublishedTableName;
        this.commentHiddenTableName = eventBoardCommentHiddenTableName;
        this.commentPublishedTableName = eventBoardCommentPublishedTableName;
        this.authorIndexName = eventBoardAuthorIndexName;
        this.hostIndexName = eventBoardHostIndexName;
        this.indexIndexName = eventBoardIndexIndexName;
        this.commentParentIdIndexName = eventBoardCommentParentIdIndexName;

        if ("dev".equals(System.getenv("SPRING_PROFILES_ACTIVE")) &&
                "true".equals(System.getenv("IS_MANUAL_TESTING"))) {
            // mirror prod tables if booting up locally for manual testing
            ddbAdapter.createOnDemandTableIfNotExists(eventBoardCommentPublishedTableName, Comment.class);
            ddbAdapter.createOnDemandTableIfNotExists(eventBoardCommentHiddenTableName, Comment.class);
            ddbAdapter.createOnDemandTableIfNotExists(eventBoardEventPublishedTableName, Event.class);
            ddbAdapter.createOnDemandTableIfNotExists(eventBoardEventHiddenTableName, Event.class);
        }
    }

    // -----------
    // ---Event---
    // -----------
    public Event saveHiddenEvent(Event event) {
        ddbAdapter.save(eventHiddenTableName, event);

        return event;
    }

    public Event savePublishedEvent(Event event) {
        ddbAdapter.save(eventPublishedTableName, event);

        return event;
    }

    public Event getPublishedEventByIndex(long index) throws EventBoardEventNotFoundException {
        Event event = Event.builder().index(index).build();
        List<Event> eventList = ddbAdapter.queryGSI(eventPublishedTableName, indexIndexName, event, Event.class);

        if (eventList.isEmpty()) {
            log.info("Event with index {} queried but not found", index);
            throw new EventBoardEventNotFoundException("Event does not exist", index);
        }

        // TODO: add alarm here if there are multiple items mapped to the same index
        if (eventList.size() > 1) {
            log.error("Multiple event board posts are mapped to the same index. This is bad, correct it ASAP. Index: {}", index);
        }

        return eventList.get(0);
    }

    public Event getPublishedEventById(String id) throws EventBoardEventNotFoundException {
        Event event = Event.builder().id(id).build();
        List<Event> eventList = ddbAdapter.query(eventPublishedTableName, event, Event.class);

        if (eventList.isEmpty()) {
            log.info("Event with id {} queried but not found", id);
            throw new EventBoardEventNotFoundException("Event does not exist");
        }

        return eventList.get(0);
    }

    // -------------
    // ---Comment---
    // -------------
    public Comment saveHiddenComment(Comment comment) {
        ddbAdapter.save(commentHiddenTableName, comment);

        return comment;
    }

    public Comment savePublishedComment(Comment comment) {
        ddbAdapter.save(commentPublishedTableName, comment);

        return comment;
    }

    public Comment getPublishedCommentById(String id) throws EventBoardCommentNotFoundException {
        Comment comment = Comment.builder().id(id).build();
        List<Comment> commentList = ddbAdapter.query(commentPublishedTableName, comment, Comment.class);

        if (commentList.isEmpty()) {
            log.info("Comment with id {} queried but not found", id);
            throw new EventBoardCommentNotFoundException("Comment does not exist");
        }

        return commentList.get(0);
    }

    public List<Comment> getPublishedCommentsByParentId(String parentId) {
        Comment parent = Comment.builder().parentId(parentId).build();
        List<Comment> comments = ddbAdapter.queryGSI(commentPublishedTableName, commentParentIdIndexName, parent, Comment.class);
        // force paginated list to fetch all
        comments.size();

        return comments;
    }
}

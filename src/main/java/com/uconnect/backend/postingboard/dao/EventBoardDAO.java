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
    }

    // -----------
    // ---Event---
    // -----------
    public void saveHiddenEvent(Event event) {
        ddbAdapter.save(eventHiddenTableName, event);
    }

    public void savePublishedEvent(Event event) {
        ddbAdapter.save(eventPublishedTableName, event);
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
    public void saveHiddenComment(Comment comment) {
        ddbAdapter.save(commentHiddenTableName, comment);
    }

    public void savePublishedComment(Comment comment) {
        ddbAdapter.save(commentPublishedTableName, comment);
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

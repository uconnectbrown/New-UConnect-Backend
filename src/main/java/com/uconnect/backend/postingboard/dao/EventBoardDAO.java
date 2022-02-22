package com.uconnect.backend.postingboard.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.postingboard.model.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    @Autowired
    public EventBoardDAO(DdbAdapter ddbAdapter, String eventBoardEventHiddenTableName, String eventBoardEventPublishedTableName,
                         String eventBoardCommentHiddenTableName, String eventBoardCommentPublishedTableName, String eventBoardAuthorIndexName,
                         String eventBoardHostIndexName, String eventBoardIndexIndexName) {
        this.ddbAdapter = ddbAdapter;
        this.eventHiddenTableName = eventBoardEventHiddenTableName;
        this.eventPublishedTableName = eventBoardEventPublishedTableName;
        this.commentHiddenTableName = eventBoardCommentHiddenTableName;
        this.commentPublishedTableName = eventBoardCommentPublishedTableName;
        this.authorIndexName = eventBoardAuthorIndexName;
        this.hostIndexName = eventBoardHostIndexName;
        this.indexIndexName = eventBoardIndexIndexName;
    }

    public void saveHiddenEvent(Event event) {
        ddbAdapter.save(eventHiddenTableName, event);
    }

    public void savePublishedEvent(Event event) {
        ddbAdapter.save(eventPublishedTableName, event);
    }

    public Event getPublishedEventByIndex(long index) throws EventBoardEventNotFoundException {
        Event event = Event.builder().index(index).build();
        for (Event e : ddbAdapter.queryGSI(eventPublishedTableName, indexIndexName, event, Event.class)) {
            return e;
        }

        log.info("Event with index {} queried but not found", index);
        throw new EventBoardEventNotFoundException("Event does not exist", index);
    }
}
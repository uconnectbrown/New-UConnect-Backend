package com.uconnect.backend.postingboard.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.postingboard.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EventBoardDAO {

    private final DdbAdapter ddbAdapter;

    private final String eventBoardEventHiddenTableName;

    private final String eventBoardEventPublishedTableName;

    private final String eventBoardCommentHiddenTableName;

    private final String eventBoardCommentPublishedTableName;

    @Autowired
    public EventBoardDAO(DdbAdapter ddbAdapter, String eventBoardEventHiddenTableName, String eventBoardEventPublishedTableName,
                         String eventBoardCommentHiddenTableName, String eventBoardCommentPublishedTableName) {
        this.ddbAdapter = ddbAdapter;
        this.eventBoardEventHiddenTableName = eventBoardEventHiddenTableName;
        this.eventBoardEventPublishedTableName = eventBoardEventPublishedTableName;
        this.eventBoardCommentHiddenTableName = eventBoardCommentHiddenTableName;
        this.eventBoardCommentPublishedTableName = eventBoardCommentPublishedTableName;
    }

    public void saveHiddenEvent(Event event) {
        ddbAdapter.save(eventBoardEventHiddenTableName, event);
    }

    public void savePublishedEvent(Event event) {
        ddbAdapter.save(eventBoardEventPublishedTableName, event);
    }
}

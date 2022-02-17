package com.uconnect.backend.postingboard.service;

import com.uconnect.backend.postingboard.dao.CounterDAO;
import com.uconnect.backend.postingboard.dao.EventBoardDAO;
import com.uconnect.backend.postingboard.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EventBoardService {

    private static final String ANONYMOUS_AUTHOR = "Anonymous Author";
    private static final String ANONYMOUS_HOST = "Anonymous Host";

    private final EventBoardDAO eventBoardDAO;

    private final CounterDAO counterDAO;

    @Autowired
    public EventBoardService(EventBoardDAO eventBoardDAO, CounterDAO counterDAO) {
        this.eventBoardDAO = eventBoardDAO;
        this.counterDAO = counterDAO;
    }

    public void saveAnonymousEvent(Event event) {
        event.setAnonymous(true);
        event.setAuthor(ANONYMOUS_AUTHOR);
        event.setHost(ANONYMOUS_HOST);
        event.setIndex(-1);

        eventBoardDAO.saveHiddenEvent(event);
    }

    public void saveVerifiedEvent(Event event) {
        event.setAnonymous(false);
        long index = counterDAO.incrementEventBoardIndex() - 1;
        event.setIndex(index);

        eventBoardDAO.savePublishedEvent(event);
    }
}

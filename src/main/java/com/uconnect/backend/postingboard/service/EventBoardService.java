package com.uconnect.backend.postingboard.service;

import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.postingboard.dao.CounterDAO;
import com.uconnect.backend.postingboard.dao.EventBoardDAO;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class EventBoardService {

    public static final String ANONYMOUS_AUTHOR = "Anonymous Author";
    public static final String ANONYMOUS_HOST = "Anonymous Host";
    private static final int MAX_SCAN_COUNT = 50;

    private final EventBoardDAO eventBoardDAO;

    private final CounterDAO counterDAO;

    @Autowired
    public EventBoardService(EventBoardDAO eventBoardDAO, CounterDAO counterDAO) {
        this.eventBoardDAO = eventBoardDAO;
        this.counterDAO = counterDAO;
    }

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
        return eventBoardDAO.getPublishedEventByIndex(index);
    }

    public GetEventResponse getLatestPublishedEvents(long startIndex, int eventCount) {
        eventCount = Math.min(eventCount, MAX_SCAN_COUNT);
        if (startIndex < 1) {
            startIndex = Long.MAX_VALUE;
        }
        startIndex = Math.min(startIndex, counterDAO.getNextEventBoardIndex() - 1);
        List<Event> acc = new ArrayList<>(eventCount);

        while (startIndex >= 0 && acc.size() < eventCount) {
            try {
                Event result = eventBoardDAO.getPublishedEventByIndex(startIndex);
                acc.add(result);
            } catch (EventBoardEventNotFoundException ignored) {
            }

            --startIndex;
        }

        return new GetEventResponse(acc, startIndex);
    }
}

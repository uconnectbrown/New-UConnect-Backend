package com.uconnect.backend.postingboard.controller;

import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsRequest;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import com.uconnect.backend.postingboard.service.EventBoardService;
import com.uconnect.backend.security.RequestPermissionUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/event-board")
public class EventBoardController {

    private final EventBoardService eventBoardService;

    private final RequestPermissionUtility requestPermissionUtility;

    @Autowired
    public EventBoardController(EventBoardService eventBoardService, RequestPermissionUtility requestPermissionUtility) {
        this.eventBoardService = eventBoardService;
        this.requestPermissionUtility = requestPermissionUtility;
    }

    @PostMapping("/anonymous/event/new")
    public ResponseEntity<String> createNewAnonymousEvent(@Valid @RequestBody Event event) {
        eventBoardService.newAnonymousEvent(event);

        return ResponseEntity.ok("Anonymous event submitted. Please wait for one of our staff members to approve it.");
    }

    @PostMapping("/verified/event/new")
    public ResponseEntity<String> createNewVerifiedEvent(@Valid @RequestBody Event event) {
        requestPermissionUtility.authorizeUser(event.getAuthor());

        eventBoardService.newVerifiedEvent(event);

        return ResponseEntity.ok("Verified event submitted. You should see it live in just a moment.");
    }

    // letting unauthenticated users see all events for now, change to /verified to tighten up control
    @GetMapping("/anonymous/event/get")
    public Event getEventByIndex(@RequestParam long index) throws EventBoardEventNotFoundException {
        return eventBoardService.getPublishedEventByIndex(index);
    }

    @PostMapping("/anonymous/event/get-latest")
    public GetEventsResponse getLatestPublishedEvents(@RequestBody GetEventsRequest request) {
        return eventBoardService.getLatestPublishedEvents(request.getStartIndex(), request.getEventCount());
    }
}

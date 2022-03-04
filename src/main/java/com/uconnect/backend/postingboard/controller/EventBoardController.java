package com.uconnect.backend.postingboard.controller;

import com.uconnect.backend.exception.EventBoardEntityNotFoundException;
import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.exception.RepeatedEventBoardReactionException;
import com.uconnect.backend.exception.UnknownEventBoardReactionException;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsRequest;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import com.uconnect.backend.postingboard.service.EventBoardService;
import com.uconnect.backend.security.RequestPermissionUtility;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/v1/event-board")
public class EventBoardController {

    public static final String ANONYMOUS_AUTHOR = "Anonymous Author";
    public static final String ANONYMOUS_HOST = "Anonymous Host";

    private final EventBoardService eventBoardService;

    private final RequestPermissionUtility requestPermissionUtility;

    @Autowired
    public EventBoardController(EventBoardService eventBoardService, RequestPermissionUtility requestPermissionUtility) {
        this.eventBoardService = eventBoardService;
        this.requestPermissionUtility = requestPermissionUtility;
    }

    // -----------
    // ---Event---
    // -----------
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
    public Event getEventByIndex(@RequestParam long index,
                                 @RequestHeader(name = "Username", required = false) String viewerUsername) throws EventBoardEventNotFoundException {
        if (StringUtils.isEmpty(viewerUsername)) {
            viewerUsername = EventBoardService.ANONYMOUS_AUTHOR;
        }

        return eventBoardService.getPublishedEventByIndex(index, viewerUsername);
    }

    @PostMapping("/anonymous/event/get-latest")
    public GetEventsResponse getLatestPublishedEvents(@RequestBody GetEventsRequest request,
                                                      @RequestHeader(name = "Username", required = false) String viewerUsername) {
        if (StringUtils.isEmpty(viewerUsername)) {
            viewerUsername = EventBoardService.ANONYMOUS_AUTHOR;
        }

        return eventBoardService.getLatestPublishedEvents(request.getStartIndex(), request.getEventCount(), viewerUsername);
    }

    // -------------
    // ---Comment---
    // -------------
    @PostMapping("/anonymous/comment/new")
    public ResponseEntity<String> createNewAnonymousComment(@Valid @RequestBody Comment comment) throws EventBoardEntityNotFoundException {
        eventBoardService.newAnonymousComment(comment);

        return ResponseEntity.ok("Anonymous comment submitted. Please wait for one of our staff members to approve it.");
    }

    @PostMapping("/verified/comment/new")
    public ResponseEntity<String> createNewVerifiedComment(@Valid @RequestBody Comment comment) throws EventBoardEntityNotFoundException {
        requestPermissionUtility.authorizeUser(comment.getAuthor());

        eventBoardService.newVerifiedComment(comment);

        return ResponseEntity.ok("Verified comment submitted. You should see it live in just a moment.");
    }

    @GetMapping("/verified/react")
    public ResponseEntity<String> reactToEventOrComment(@RequestParam String id, @RequestParam String reactionType) throws EventBoardEntityNotFoundException {
        String reactor = ((User) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getUsername();
        try {
            eventBoardService.reactToEventOrComment(id, reactionType, reactor);
        } catch (RepeatedEventBoardReactionException e) {
            log.warn("User \"{}\" might be abusing reaction endpoints (repeated reactions), keep an eye on him", reactor);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    String.format("You have already reacted to this entity with reaction type: %s", reactionType));
        } catch (UnknownEventBoardReactionException e) {
            log.warn("User \"{}\" might be abusing reaction endpoints (unknown reaction type), keep an eye on him", reactor);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    String.format("Reaction type: %s not allowed", reactionType));
        }

        return ResponseEntity.ok(String.format("Reacted to %s with type: %s", id, reactionType));
    }
}

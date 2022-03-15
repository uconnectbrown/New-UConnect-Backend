package com.uconnect.backend.postingboard.service;

import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.exception.EventBoardCommentNotFoundException;
import com.uconnect.backend.exception.EventBoardEntityNotFoundException;
import com.uconnect.backend.exception.EventBoardEventNotFoundException;
import com.uconnect.backend.exception.RepeatedEventBoardReactionException;
import com.uconnect.backend.exception.UnknownEventBoardReactionException;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.postingboard.dao.CounterDAO;
import com.uconnect.backend.postingboard.dao.EventBoardDAO;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import com.uconnect.backend.postingboard.model.ReactionCollection;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class EventBoardService {

    public static final String ANONYMOUS_AUTHOR = "Anonymous Author";
    // TODO: fill
    public static final String ANONYMOUS_AUTHOR_IMAGE_URL = "https://i.imgur.com/op52w9Q.jpeg";
    public static final String ANONYMOUS_HOST = "Anonymous Host";
    public static final int MAX_SCAN_COUNT = 50;

    // ddb does not allow empty string sets, so we must include the empty string to avoid null
    public static final ReactionCollection EMPTY_REACTION_COLLECTION = ReactionCollection.builder()
            .likeCount(0)
            .likeUsernames(ImmutableSet.of(""))
            .interestedUsernames(ImmutableSet.of(""))
            .interestedCount(0)
            .loveUsernames(ImmutableSet.of(""))
            .loveCount(0)
            .build();

    private static final Set<String> EMPTY_STRING_SET = ImmutableSet.of();

    private final EventBoardDAO eventBoardDAO;

    private final CounterDAO counterDAO;

    private final UserDAO userDAO;

    @Autowired
    public EventBoardService(EventBoardDAO eventBoardDAO, CounterDAO counterDAO, UserDAO userDAO) {
        this.eventBoardDAO = eventBoardDAO;
        this.counterDAO = counterDAO;
        this.userDAO = userDAO;
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
        event.setReactions(EMPTY_REACTION_COLLECTION);

        eventBoardDAO.saveHiddenEvent(event);
    }

    public void newVerifiedEvent(Event event) {
        event.setTimestamp(new Date());
        event.setAnonymous(false);
        long index = counterDAO.incrementEventBoardIndex() - 1;
        event.setIndex(index);
        event.setReactions(EMPTY_REACTION_COLLECTION);

        eventBoardDAO.savePublishedEvent(event);
    }

    public Event getPublishedEventByIndex(long index, String viewUsername) throws EventBoardEventNotFoundException {
        Event event = eventBoardDAO.getPublishedEventByIndex(index);
        assembleEvent(event, viewUsername);

        return event;
    }

    public GetEventsResponse getLatestPublishedEvents(long startIndex, int eventCount, String viwerUsername) {
        eventCount = Math.min(eventCount, MAX_SCAN_COUNT);
        eventCount = Math.max(eventCount, 0);
        if (startIndex < 0) {
            startIndex = Long.MAX_VALUE;
        }
        startIndex = Math.min(startIndex, counterDAO.getNextEventBoardIndex() - 1);
        List<Event> acc = new ArrayList<>(eventCount);

        while (startIndex >= 0 && acc.size() < eventCount) {
            try {
                Event result = eventBoardDAO.getPublishedEventByIndex(startIndex);
                assembleEvent(result, viwerUsername);
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
    public void newAnonymousComment(Comment comment) throws EventBoardEntityNotFoundException {
        String parentId = comment.getParentId();
        // TODO: fix this and save the parent comment as isCommentPresent
        verifyEntityExists(parentId);

        comment.setTimestamp(new Date());
        comment.setAnonymous(true);
        comment.setAuthor(ANONYMOUS_AUTHOR);
        comment.setReactions(EMPTY_REACTION_COLLECTION);
        eventBoardDAO.saveHiddenComment(comment);
    }

    public void newVerifiedComment(Comment comment) throws EventBoardEntityNotFoundException {
        String parentId = comment.getParentId();
        // TODO: fix this and save the parent comment as isCommentPresent
        verifyEntityExists(parentId);

        comment.setTimestamp(new Date());
        comment.setAnonymous(false);
        comment.setReactions(EMPTY_REACTION_COLLECTION);
        eventBoardDAO.savePublishedComment(comment);
    }

    public List<Comment> getPublishedCommentsByParentId(String parentId, String viewerUsername) {
        List<Comment> comments = eventBoardDAO.getPublishedCommentsByParentId(parentId);
        populateChildrenComments(comments, viewerUsername);

        return comments;
    }

    synchronized public void reactToEventOrComment(String id, String type, String reactorUsername) throws EventBoardEntityNotFoundException, RepeatedEventBoardReactionException, UnknownEventBoardReactionException {
        Pair<Event, Comment> pair = verifyEntityExists(id);
        ReactionCollection reactions;
        if (pair.getKey() != null) {
            // handling event
            reactions = pair.getKey().getReactions();
        } else {
            // handling comment
            reactions = pair.getValue().getReactions();
        }

        type = StringUtils.toRootUpperCase(type);
        // handle reaction
        switch (type) {
            case "LIKE":
                if (reactions.getLikeUsernames().contains(reactorUsername)) {
                    throw new RepeatedEventBoardReactionException();
                }
                reactions.getLikeUsernames().add(reactorUsername);
                reactions.setLikeCount(reactions.getLikeCount() + 1);

                break;
            case "LOVE":
                if (reactions.getLoveUsernames().contains(reactorUsername)) {
                    throw new RepeatedEventBoardReactionException();
                }
                reactions.getLoveUsernames().add(reactorUsername);
                reactions.setLoveCount(reactions.getLoveCount() + 1);

                break;
            case "INTERESTED":
                if (reactions.getInterestedUsernames().contains(reactorUsername)) {
                    throw new RepeatedEventBoardReactionException();
                }
                reactions.getInterestedUsernames().add(reactorUsername);
                reactions.setInterestedCount(reactions.getInterestedCount() + 1);

                break;
            default:
                throw new UnknownEventBoardReactionException();
        }

        if (pair.getKey() != null) {
            // update event
            eventBoardDAO.savePublishedEvent(
                    Event.builder()
                            .id(pair.getKey().getId())
                            .timestamp(pair.getKey().getTimestamp())
                            .reactions(reactions)
                            .build()
            );
        } else {
            // update comment
            eventBoardDAO.savePublishedComment(
                    Comment.builder()
                            .id(pair.getValue().getId())
                            .timestamp(pair.getValue().getTimestamp())
                            .reactions(reactions)
                            .build()
            );
        }
    }

    /**
     * Return a pair with either an Event or a comment in it, the entity not found will be null. If neither is found, throws exception
     */
    private Pair<Event, Comment> verifyEntityExists(String parentId) throws EventBoardEntityNotFoundException {
        try {
            // found as an event
            Event foundEvent = eventBoardDAO.getPublishedEventById(parentId);
            return Pair.of(foundEvent, null);
        } catch (EventBoardEventNotFoundException e) {
            try {
                // found as a comment
                Comment foundComment = eventBoardDAO.getPublishedCommentById(parentId);
                return Pair.of(null, foundComment);
            } catch (EventBoardCommentNotFoundException ce) {
                log.info("Event board entity with id \"{}\" queried but not found", parentId);
                throw new EventBoardEntityNotFoundException();
            }
        }
    }

    private void populateChildrenComments(List<Comment> comments, String viewerUsername) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (Comment comment : comments) {
            if (comment != null) {
                comment.setAuthorInfo(getAuthorInfo(comment.getAuthor()));
                eraseReactorUsernames(comment.getReactions(), viewerUsername);

                if (comment.isCommentPresent()) {
                    List<Comment> children = eventBoardDAO.getPublishedCommentsByParentId(comment.getId());
                    comment.setComments(children);

                    populateChildrenComments(children, viewerUsername);
                }
            }
        }
    }

    private void assembleEvent(Event event, String viewerUsername) {
        event.setAuthorInfo(getAuthorInfo(event.getAuthor()));
        event.setComments(getPublishedCommentsByParentId(event.getId(), viewerUsername));
        eraseReactorUsernames(event.getReactions(), viewerUsername);
    }

    private User getAuthorInfo(String username) {
        User user = User.builder()
                .username(username)
                .firstName(ANONYMOUS_AUTHOR)
                .imageUrl(ANONYMOUS_AUTHOR_IMAGE_URL)
                .build();
        if (ANONYMOUS_AUTHOR.equals(username)) {
            return user;
        }

        try {
            User rawUser = userDAO.getUserByUsername(username);
            rawUser.setPassword("");

            user = rawUser;
        } catch (UserNotFoundException e) {
            log.warn("Username \"{}\" not found while trying to fill an Event's author info, someone should verify this " +
                    "user was actually deleted", username);
        }

        return user;
    }

    private void eraseReactorUsernames(ReactionCollection reactions, String viewerUsername) {
        if (reactions == null) {
            return;
        }

        Set<String> onlyViewerUsername = ImmutableSet.of(viewerUsername);

        reactions.setLikeUsernames(reactions.getLikeUsernames() != null &&
                reactions.getLikeUsernames().contains(viewerUsername) ? onlyViewerUsername : EMPTY_STRING_SET);
        reactions.setLoveUsernames(reactions.getLoveUsernames() != null &&
                reactions.getLoveUsernames().contains(viewerUsername) ? onlyViewerUsername : EMPTY_STRING_SET);
        reactions.setInterestedUsernames(reactions.getInterestedUsernames() != null &&
                reactions.getInterestedUsernames().contains(viewerUsername) ? onlyViewerUsername : EMPTY_STRING_SET);
    }
}

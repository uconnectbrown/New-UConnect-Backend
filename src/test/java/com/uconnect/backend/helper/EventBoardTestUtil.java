package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsRequest;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import com.uconnect.backend.postingboard.model.ReactionCollection;
import com.uconnect.backend.user.model.User;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventBoardTestUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static ResultActions submitEventAnonymous(MockMvc mockMvc, Event event) throws Exception {
        String requestBody = MAPPER.writeValueAsString(event);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/anonymous/event/new")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions submitEventVerified(MockMvc mockMvc, Event event, String username, String token) throws Exception {
        String requestBody = MAPPER.writeValueAsString(event);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/verified/event/new")
                        .header("Username", username)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions submitCommentAnonymous(MockMvc mockMvc, Comment comment) throws Exception {
        String requestBody = MAPPER.writeValueAsString(comment);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/anonymous/comment/new")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions submitCommentVerified(MockMvc mockMvc, Comment comment, String username, String token) throws Exception {
        String requestBody = MAPPER.writeValueAsString(comment);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/verified/comment/new")
                        .header("Username", username)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static Event getEventObjByIndex(MockMvc mockMvc, long index, String username, String token) throws Exception {
        MvcResult result = getEventByIndex(mockMvc, index, username, token)
                .andExpect(status().isOk())
                .andReturn();

        return MAPPER.readValue(result.getResponse().getContentAsString(), Event.class);
    }

    /**
     * Username and token are currently ignored since /get endpoints are temporarily open to public.
     * Add them as headers once/if these endpoints become authenticated.
     */
    public static ResultActions getEventByIndex(MockMvc mockMvc, long index, String username, String token) throws Exception {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
                .get(String.format("/v1/event-board/anonymous/event/get?index=%s", index))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        if (StringUtils.isNotBlank(username)) {
            builder.header("Username", username);
        }
        if (StringUtils.isNotBlank(token)) {
            builder.header("Authorization", String.format("%s %s", "Bearer", token));
        }

        return mockMvc.perform(builder);
    }

    public static GetEventsResponse getLatestEventsResponse(MockMvc mockMvc, long startIndex, int count, String username, String token) throws Exception {
        MvcResult result = getLatestEvents(mockMvc, startIndex, count, username, token)
                .andReturn();

        return MAPPER.readValue(result.getResponse().getContentAsString(), GetEventsResponse.class);
    }

    /**
     * Username and token are currently ignored since /get endpoints are temporarily open to public.
     * Add them as headers once/if these endpoints become authenticated.
     */
    public static ResultActions getLatestEvents(MockMvc mockMvc, long startIndex, int count, String username, String token) throws Exception {
        String body = MAPPER.writeValueAsString(new GetEventsRequest(startIndex, count));

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/anonymous/event/get-latest")
                        .header("Username", username)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions react(MockMvc mockMvc, String id, String type, String username, String token) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format("/v1/event-board/verified/react?id=%s&reactionType=%s", id, type))
                        .header("Username", username)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static void verifySameEventsSkipReactions(Event e1, Event e2) {
        assertEquals(e1.getTitle(), e2.getTitle());
        assertEquals(e1.getAuthor(), e2.getAuthor());
        haveSameAuthorInfo(e1.getAuthorInfo(), e2.getAuthorInfo());
        assertEquals(e1.getHost(), e2.getHost());
        assertEquals(e1.getDescription(), e2.getDescription());
        // ignore last five digits due to time precision
        assertEquals(e1.getTimestamp().getTime() / 10_000, e2.getTimestamp().getTime() / 10_000);
        assertEquals(e1.getStartTime(), e2.getStartTime());
        assertEquals(e1.isAnonymous(), e2.isAnonymous());
        haveSameComments(e1.getComments(), e2.getComments());
    }

    public static void verifySameCommentsSkipReactions(Comment c1, Comment c2) {
        assertEquals(c1.getAuthor(), c2.getAuthor());
        assertEquals(c1.getContent(), c2.getContent());
        assertEquals(c1.isAnonymous(), c2.isAnonymous());
        // ignore last 6 digits due to time precision
        assertEquals(c1.getTimestamp().getTime() / 100_000, c2.getTimestamp().getTime() / 100_000);
        haveSameAuthorInfo(c1.getAuthorInfo(), c2.getAuthorInfo());
        haveSameComments(c1.getComments(), c2.getComments());
    }

    public static void haveSameComments(List<Comment> l1, List<Comment> l2) {
        if (l1 == null && l2 == null) {
            return;
        }
        if (l1 == null || l2 == null) {
            fail();
        }

        if (l1.size() != l2.size()) {
            fail();
        }

        l1 = new ArrayList<>(l1);
        l1.sort(Comparator.comparing(Comment::getContent));
        l2 = new ArrayList<>(l2);
        l2.sort(Comparator.comparing(Comment::getContent));

        for (int i = 0; i < l1.size(); i++) {
            Comment c1 = l1.get(i);
            Comment c2 = l2.get(i);

            verifySameCommentsSkipReactions(c1, c2);
        }
    }

    public static void haveSameReactions(ReactionCollection r1, ReactionCollection r2) {
        if (r1 == null && r2 == null) {
            return;
        }
        if (r1 == null || r2 == null) {
            fail();
        }

        assertEquals(r1.getLikeUsernames(), r2.getLikeUsernames());
        assertEquals(r1.getLikeCount(), r2.getLikeCount());
        assertEquals(r1.getLoveUsernames(), r2.getLoveUsernames());
        assertEquals(r1.getLoveCount(), r2.getLoveCount());
        assertEquals(r1.getInterestedUsernames(), r2.getInterestedUsernames());
        assertEquals(r1.getInterestedCount(), r2.getInterestedCount());
    }

    private static void haveSameAuthorInfo(User a1, User a2) {
        if (a1 == null && a2 == null) {
            return;
        }
        if (a1 == null || a2 == null) {
            fail();
        }
        assertEquals(a1.getUsername(), a2.getUsername());
        assertEquals(a1.getFirstName(), a2.getFirstName());
        assertEquals(a1.getPassword(), a2.getPassword());
        assertEquals(a1.getImageUrl(), a2.getImageUrl());
    }
}

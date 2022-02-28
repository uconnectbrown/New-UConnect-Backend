package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.postingboard.model.Comment;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsRequest;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
                .andReturn();

        return MAPPER.readValue(result.getResponse().getContentAsString(), Event.class);
    }

    /**
     * Username and token are currently ignored since /get endpoints are temporarily open to public.
     * Add them as headers once/if these endpoints become authenticated.
     */
    public static ResultActions getEventByIndex(MockMvc mockMvc, long index, String username, String token) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format("/v1/event-board/anonymous/event/get?index=%s", index))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
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
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static void verifySameEvents(Event e1, Event e2) {
        assertEquals(e1.getTitle(), e2.getTitle());
        assertEquals(e1.getAuthor(), e2.getAuthor());
        assertEquals(e1.getHost(), e2.getHost());
        assertEquals(e1.getDescription(), e2.getDescription());
        // ignore last five digits due to time precision
        assertEquals(e1.getTimestamp().getTime() / 10_000, e2.getTimestamp().getTime() / 10_000);
        assertEquals(e1.getStartTime(), e2.getStartTime());
        assertEquals(e1.isAnonymous(), e2.isAnonymous());
        haveSameComments(e1.getComments(), e2.getComments());
    }

    public static void verifySameComments(Comment c1, Comment c2) {
        assertEquals(c1.getAuthor(), c2.getAuthor());
        assertEquals(c1.getContent(), c2.getContent());
        assertEquals(c1.isAnonymous(), c2.isAnonymous());
        // ignore last 6 digits due to time precision
        assertEquals(c1.getTimestamp().getTime() / 100_000, c2.getTimestamp().getTime() / 100_000);
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

        for (int i = 0; i < l1.size(); i++) {
            Comment c1 = l1.get(i);
            Comment c2 = l2.get(i);

            verifySameComments(c1, c2);
        }
    }
}

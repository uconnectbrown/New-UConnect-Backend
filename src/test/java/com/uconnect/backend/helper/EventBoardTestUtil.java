package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.postingboard.model.Event;
import com.uconnect.backend.postingboard.model.GetEventsRequest;
import com.uconnect.backend.postingboard.model.GetEventsResponse;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EventBoardTestUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static ResultActions submitAnonymous(MockMvc mockMvc, Event event) throws Exception {
        String requestBody = MAPPER.writeValueAsString(event);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/event-board/anonymous/event/new")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static ResultActions submitVerified(MockMvc mockMvc, Event event, String username, String token) throws Exception {
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

    public static Event getEventObjByIndex(MockMvc mockMvc, int index, String username, String token) throws Exception {
        MvcResult result = getEventByIndex(mockMvc, index, username, token)
                .andReturn();

        return MAPPER.readValue(result.getResponse().getContentAsString(), Event.class);
    }

    /**
     * Username and token are currently ignored since /get endpoints are temporarily open to public.
     * Add them as headers once/if these endpoints become authenticated.
     */
    public static ResultActions getEventByIndex(MockMvc mockMvc, int index, String username, String token) throws Exception {
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
    }
}

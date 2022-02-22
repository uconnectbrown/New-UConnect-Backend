package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.postingboard.model.Event;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
}

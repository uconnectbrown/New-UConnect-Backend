package com.uconnect.backend.connect.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(ConnectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TestCheckStatus extends BaseConnectControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private boolean init = false;

    private User current;

    private User other;

    private Map<String, String> request;

    @BeforeEach
    public void setup() {
        if (!init) {
            current = MockData.generateValidUser();
            other = MockData.generateValidUser();
            request = new HashMap<>();
            request.put("current", current.getUsername());
            request.put("other", other.getUsername());

            init = true;
        }
    }

    private MvcResult testCheckStatus(Map<String, String> request, ResultMatcher status,
            String msg) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/checkStatus")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testHasIncoming() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(3);
        String msg = current.getUsername() + " has an incoming request from "
                + other.getUsername();
        testCheckStatus(request, status().isOk(), msg);
    }

    @Test
    public void testHasOutgoing() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(2);
        String msg = current.getUsername() + " has an outgoing request to "
                + other.getUsername();
        testCheckStatus(request, status().isOk(), msg);
    }

    @Test
    public void testConnected() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(1);
        String msg = current.getUsername() + " and "
                + other.getUsername() + " are connected.";
        testCheckStatus(request, status().isOk(), msg);
    }

    @Test
    public void testNoRelation() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(0);
        String msg = "No relation exists between " + current.getUsername()
                + " and " + other.getUsername();
        testCheckStatus(request, status().isOk(), msg);
    }

    @Test
    public void testUserNotFound() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(-1);
        String msg = "User not found";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/checkStatus")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testUnexpectedException() throws Exception {
        when(connectService.checkStatus(current.getUsername(), other.getUsername()))
                .thenReturn(-2);
        String msg = "Unexpected error";
        testCheckStatus(request, status().isInternalServerError(), msg);
    }
}

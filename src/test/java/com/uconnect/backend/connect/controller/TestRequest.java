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
public class TestRequest extends BaseConnectControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private boolean init = false;

    private User sender;

    private User receiver;

    private Map<String, String> request;

    @BeforeEach
    public void setup() {
        if (!init) {
            sender = MockData.generateValidUser();
            receiver = MockData.generateValidUser();
            request = new HashMap<>();
            request.put("sender", sender.getUsername());
            request.put("receiver", receiver.getUsername());

            init = true;
        }
    }

    private MvcResult testRequest(Map<String, String> request, ResultMatcher status, String msg)
            throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/request")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testValidRequest() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(0);
        String msg = "Successfully created a request from "
                + sender.getUsername() + " to " + receiver.getUsername();
        testRequest(request, status().isOk(), msg);
    }

    @Test
    public void testRequestAlreadySent() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-1);
        String msg = "Operation unsuccessful: " + sender.getUsername()
                + " has already sent a request to " + receiver.getUsername();
        testRequest(request, status().isBadRequest(), msg);
    }

    @Test
    public void testRequestAlreadyReceived() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-2);
        String msg = "Operation unsuccessful: " + receiver.getUsername()
                + " has already received a request from " + sender.getUsername()
                + ". This should not have happened.";
        testRequest(request, status().isBadRequest(), msg);
    }

    @Test
    public void testNotEnoughRequests() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-3);
        String msg = "Operation unsuccessful: " + sender.getUsername()
                + " has an insufficient number of requests";
        testRequest(request, status().isBadRequest(), msg);
    }

    @Test
    public void testUserNotFound() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-4);
        String msg = "Operation unsuccessful: user not found";
        testRequest(request, status().isNotFound(), msg);
    }

    @Test
    public void testUnexpectedException() throws Exception {
        when(connectService.request(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-5);
        String msg = "Unexpected error";
        testRequest(request, status().isInternalServerError(), msg);
    }
}

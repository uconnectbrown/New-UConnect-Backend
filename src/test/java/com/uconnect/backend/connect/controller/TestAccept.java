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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WebMvcTest(ConnectController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TestAccept extends BaseConnectControllerUnitTest {
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

    @Test
    public void testValidAccept() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(0);
        String msg = "Successfully accepted request from " + sender.getUsername()
                + " to " + receiver.getUsername();
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testRequestNeverReceived() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-1);
        String msg = "Operation unsuccessful: " + receiver.getUsername()
                + " has no request from " + sender.getUsername() +
                ". This should not have happened.";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testRequestNeverSent() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-2);
        String msg = "Operation unsuccessful: " + sender.getUsername()
                + " has not sent a request to " + receiver.getUsername()
                + ". This should not have happened.";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testReceiverAlreadyConnected() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-3);
        String msg = "Operation unsuccessful: " + receiver.getUsername()
                + " is already connected with " + sender.getUsername()
                + ". This should not have happened.";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(msg))
                .andReturn();
    }

    // for code coverage: a connection should be reflexive
    @Test
    public void testSenderAlreadyConnected() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-4);
        String msg = "Operation unsuccessful: " + sender.getUsername()
                + " is already connected with " + receiver.getUsername()
                + ". This should not have happened.";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testTooManyRequests() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-5);
        String msg = "Operation unsuccessful: " + sender.getUsername()
                + " has too many requests. This should not have happened.";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testUserNotFound() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-6);
        String msg = "User not found";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(msg))
                .andReturn();
    }

    @Test
    public void testUnexpectedException() throws Exception {
        when(connectService.accept(sender.getUsername(), receiver.getUsername()))
                .thenReturn(-7);
        String msg = "Unexpected error";
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/connect/accept")
                        .content(mapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(msg))
                .andReturn();
    }
}

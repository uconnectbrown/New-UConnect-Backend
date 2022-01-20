package com.uconnect.backend.user.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

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

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TestGetPending extends BaseUserControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();

    private List<User> users;

    private final int NUM_USERS = 10;

    @BeforeEach
    public void setup() {
        // Populate users
        users = new ArrayList<>();
        for (int i = 0; i < NUM_USERS; i++) {
            users.add(MockData.generateValidUser());
        }

        // Set pending for first user
        Set<String> pending = new HashSet<>();
        pending.add(users.get(1).getUsername());
        pending.add(users.get(3).getUsername());
        pending.add(users.get(8).getUsername());
        users.get(0).setPending(pending);
    }

    private MvcResult testGetPending(String username, ResultMatcher status,
            Set<String> pending) throws Exception {
        if (pending != null) {
            return mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/user/getPending")
                            .header("Username", username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status)
                    .andExpect(content().json(mapper.writeValueAsString(pending)))
                    .andReturn();
        } else {
            return mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/user/getPending")
                            .header("Username", username)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status)
                    .andExpect(content().string(""))
                    .andReturn();
        }
    }

    @Test
    public void testValid() throws Exception {
        User currentUser = users.get(0);
        when(userService.getPending(currentUser.getUsername()))
                .thenReturn(currentUser.getPending());
        MvcResult res = testGetPending(currentUser.getUsername(), status().isOk(),
                currentUser.getPending());
        assertEquals(mapper.writeValueAsString(currentUser.getPending()),
                res.getResponse().getContentAsString());
    }

    @Test
    public void testNotFound() throws Exception {
        User currentUser = users.get(0);
        when(userService.getPending(currentUser.getUsername()))
                .thenReturn(null);
        testGetPending(currentUser.getUsername(), status().isNotFound(), null);
    }

    @Test
    public void testUnexpectedException() throws Exception {
        User currentUser = users.get(0);
        when(userService.getPending(currentUser.getUsername()))
                .thenThrow(RuntimeException.class);
        testGetPending(currentUser.getUsername(), status().isInternalServerError(), null);
    }
}

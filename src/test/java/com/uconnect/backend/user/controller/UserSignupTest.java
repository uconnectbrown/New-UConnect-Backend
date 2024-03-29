package com.uconnect.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
// use "addFilters=false" to circumvent security
@AutoConfigureMockMvc(addFilters = false)
public class UserSignupTest extends BaseUserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();
    private User user;
    private final String validUsername = "test@email.com";
    private final String rawPassword = "tellMeASecret";
    private final String firstName = "John";
    private final String lastName = "Doe";
    private final String classYear = "2022";

    @BeforeEach
    public void setup() {
        user = User.builder()
                .username(validUsername)
                .firstName(firstName)
                .lastName(lastName)
                .password(rawPassword)
                .classYear(classYear)
                .build();
    }

    @Test
    public void testNotNullId() throws Exception {
        user.setId("naughty69");
        String requestBody = mapper.writeValueAsString(user);
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/signup/createNewUserTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("New users are assigned random IDs")))
                .andReturn();
    }

    @Test
    public void testNullPassword() throws Exception {
        user.setPassword(null);
        String requestBody = mapper.writeValueAsString(user);
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/signup/createNewUserTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("password cannot be null")))
                .andReturn();
    }
}

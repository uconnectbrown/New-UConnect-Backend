package com.uconnect.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
public class UserAuthenticationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private final ObjectMapper mapper = new ObjectMapper();
    private User user;
    private final String validId = "0";
    private final String validUsername = "test@email.com";
    private final String validPassword = "tellMeASecret";
    private final String nonExistentUsername = "no-such@user.edu";
    private final String badPassword = "hushMyChildItsChristmas";

    @BeforeEach
    public void setup() {
        user = User.builder()
                .id(validId)
                .username(validUsername)
                .build();
    }

    @Test
    public void testTraditionalAuthSuccess() throws Exception {
        when(userService.loadUserByUsername(validUsername)).thenReturn(user);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(validUsername, validPassword)))
                .thenReturn(null);

        JwtRequest request = new JwtRequest(validUsername, validPassword);
        String requestBody = mapper.writeValueAsString(request);

        // obtain token
        MvcResult result = mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/authenticate/authenticateTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        String token = response.getJwtToken();

        // test returned token
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/")
                        .header("Username", validUsername)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You are logged in, John Cena!")));
    }

    @Test
    public void testTraditionalAuthUsernameDoesNotExist() throws Exception {
        when(userService.loadUserByUsername(validUsername)).thenReturn(user);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(nonExistentUsername, validPassword)))
                .thenThrow(new BadCredentialsException(""));

        JwtRequest request = new JwtRequest(nonExistentUsername, validPassword);
        String requestBody = mapper.writeValueAsString(request);

        // fail to obtain token
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/authenticate/authenticateTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")));
    }

    @Test
    public void testTraditionalAuthIncorrectPassword() throws Exception {
        when(userService.loadUserByUsername(validUsername)).thenReturn(user);
        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(validUsername, badPassword)))
                .thenThrow(new BadCredentialsException(""));

        JwtRequest request = new JwtRequest(validUsername, badPassword);
        String requestBody = mapper.writeValueAsString(request);

        // fail to obtain token
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/authenticate/authenticateTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")));
    }
}

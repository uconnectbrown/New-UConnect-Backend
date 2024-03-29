package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.user.model.User;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationTestUtil {
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static ResultActions createUserTraditional(MockMvc mockMvc, User user) throws Exception {
        String requestBody = MAPPER.writeValueAsString(user);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/signup/createNewUserTraditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static void createUserTraditionalSuccess(MockMvc mockMvc, User user) throws Exception {
        createUserTraditional(mockMvc, user)
                .andExpect(status().isAccepted())
                .andReturn();
    }

    public static ResultActions loginTraditional(MockMvc mockMvc, JwtRequest request) throws Exception {
        String requestBody = MAPPER.writeValueAsString(request);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/authenticate/traditional")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    public static void verifyAuthenticationSuccess(MockMvc mockMvc, String token, String username) throws Exception {
        verifyAuthentication(mockMvc, token, username)
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("You are logged in, John Cena!")))
                .andReturn();
    }

    public static void verifyAuthenticationFailure(MockMvc mockMvc, String token, String username) throws Exception {
        verifyAuthentication(mockMvc, token, username)
                .andExpect(status().isForbidden())
                .andReturn();
    }

    public static ResultActions loginOAuth(MockMvc mockMvc, String registrationId, String oAuthRequestString) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post(String.format("/v1/user/authenticate/oauth/%s", registrationId))
                        .content(oAuthRequestString)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }

    private static ResultActions verifyAuthentication(MockMvc mockMvc, String token, String username) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get("/")
                        .header("Username", username)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }
}

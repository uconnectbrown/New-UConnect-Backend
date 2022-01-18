package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.user.model.User;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserTestUtil {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String getTokenForTraditionalUser(MockMvc mockMvc, User user, boolean isNewUser) throws Exception {
        String requestBody;

        if (isNewUser) {
            requestBody = mapper.writeValueAsString(user);
            AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody);
        }

        JwtRequest request = new JwtRequest(user.getUsername(), user.getPassword());
        requestBody = mapper.writeValueAsString(request);
        MvcResult result = AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse tokenResponse = mapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);

        return tokenResponse.getJwtToken();
    }

    public static ResultActions getUser(MockMvc mockMvc, String requestUsername, String queryUsername, String token) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .post(String.format("/v1/user/%s", queryUsername))
                        .header("Username", requestUsername)
                        .header("Authorization", String.format("%s %s", "Bearer", token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON));
    }
}

package com.uconnect.backend.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.user.model.EmailVerification;
import com.uconnect.backend.user.model.User;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserTestUtil {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static String getTokenForTraditionalUser(MockMvc mockMvc, User user, boolean isNewUser,
                                                    DdbAdapter ddbAdapter, String userTableName) throws Exception {
        if (isNewUser) {
            AuthenticationTestUtil.createUserTraditional(mockMvc, user);
            verifyUserHack(ddbAdapter, user.getUsername(), userTableName);
        }

        JwtRequest request = new JwtRequest(user.getUsername(), user.getPassword());
        MvcResult result = AuthenticationTestUtil.loginTraditional(mockMvc, request)
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse tokenResponse = MAPPER.readValue(result.getResponse().getContentAsString(), JwtResponse.class);

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

    /**
     * Directly modify the database to verify a user. User with caution!
     *
     * @param ddbAdapter
     * @param username
     * @param userTableName
     * @throws UserNotFoundException
     */
    public static void verifyUserHack(DdbAdapter ddbAdapter, String username, String userTableName) throws UserNotFoundException {
        User user = ddbAdapter.findByUsername(username);
        user.setVerified(true);
        ddbAdapter.save(userTableName, user);
    }

    public static ResultActions verifyUser(MockMvc mockMvc, EmailVerification verification) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format("/v1/user/authenticate/emailVerification/%s?code=%s",
                                verification.getEmailAddress(),
                                verification.getVerificationCode()))
                        .accept(MediaType.APPLICATION_JSON));
    }
}

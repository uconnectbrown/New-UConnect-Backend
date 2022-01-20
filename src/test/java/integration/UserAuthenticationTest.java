package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserAuthenticationTest extends BaseIntTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private User user;
    private final String validUsername = "test@brown.edu";
    private final String invalidDomainUsername = "tester@vassar.edu";
    private final String validPassword = "tellMeASecret";
    private final String classYear = "2021";
    private final String nonExistentUsername = "no-such@brown.edu";
    private final String badPassword = "hushMyChildItsChristmas";

    @BeforeEach
    public void setup() {
        user = User.builder()
                .username(validUsername)
                .password(validPassword)
                .firstName(validUsername)
                .lastName(validUsername)
                .classYear(classYear)
                .build();

        setupDdb();
    }

    @Test
    @Order(1)
    public void testTraditionalAuthSuccess() throws Exception {
        MvcResult result;

        // register test user
        String requestBody = mapper.writeValueAsString(user);
        AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody)
                .andExpect(status().isAccepted())
                .andReturn();

        // verify test user
        UserTestUtil.verifyUser(ddbAdapter, user.getUsername(), userTableName);

        JwtRequest request = new JwtRequest(validUsername, validPassword);
        requestBody = mapper.writeValueAsString(request);
        // obtain token
        result = AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        String token = response.getJwtToken();

        AuthenticationTestUtil.verifyAuthenticationSuccess(mockMvc, token, validUsername);
    }

    @Test
    @Order(2)
    public void testTraditionalAuthUsernameDoesNotExist() throws Exception {
        JwtRequest request = new JwtRequest(nonExistentUsername, validPassword);
        String requestBody = mapper.writeValueAsString(request);

        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")))
                .andReturn();
    }

    @Test
    @Order(3)
    public void testTraditionalAuthIncorrectPassword() throws Exception {
        JwtRequest request = new JwtRequest(validUsername, badPassword);
        String requestBody = mapper.writeValueAsString(request);

        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")))
                .andReturn();
    }

    @Test
    @Order(4)
    public void testTraditionalAuthInvalidEmailDomain() throws Exception {
        user.setUsername(invalidDomainUsername);

        // register test user
        String requestBody = mapper.writeValueAsString(user);
        AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(String.format("Disallowed email domain: %s", invalidDomainUsername))))
                .andReturn();

        JwtRequest request = new JwtRequest(invalidDomainUsername, user.getPassword());
        requestBody = mapper.writeValueAsString(request);
        // fail to log in
        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")))
                .andReturn();
    }

    @Test
    @Order(5)
    public void testTraditionalAuthUnverified() throws Exception {
        MvcResult result;
        String altUsername = "temp@brown.edu";
        String altPassword = "justAnother";
        user.setUsername(altUsername);
        user.setPassword(altPassword);

        // register test user
        String requestBody = mapper.writeValueAsString(user);
        AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody)
                .andExpect(status().isAccepted())
                .andReturn();

        JwtRequest request = new JwtRequest(altUsername, altPassword);
        requestBody = mapper.writeValueAsString(request);
        // obtain token
        result = AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        String token = response.getJwtToken();

        assertEquals("notVerified", token);
        AuthenticationTestUtil.verifyAuthenticationFailure(mockMvc, token, validUsername);
    }
}

package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.UConnectBackendApplication;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UConnectBackendApplication.class)
@AutoConfigureMockMvc
public class UserAuthenticationTest extends BaseIntTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private String userTableName;

    private final ObjectMapper mapper = new ObjectMapper();
    private User user;
    private final String validUsername = "test@email.com";
    private final String validPassword = "tellMeASecret";
    private final String classYear = "2021";
    private final String nonExistentUsername = "no-such@user.edu";
    private final String badPassword = "hushMyChildItsChristmas";

    @BeforeEach
    public void setup() throws InterruptedException {
        user = User.builder()
                .username(validUsername)
                .password(validPassword)
                .firstName(validUsername)
                .lastName(validUsername)
                .classYear(classYear)
                .build();

        if (ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class)) {
            // wait for the new table to become available
            Thread.sleep(10);
        }
    }

    @Test
    public void testTraditionalAuthSuccess() throws Exception {
        MvcResult result;

        // register test user
        String requestBody = mapper.writeValueAsString(user);
        AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody)
                .andExpect(status().isAccepted())
                .andReturn();

        JwtRequest request = new JwtRequest(validUsername, validPassword);
        requestBody = mapper.writeValueAsString(request);
        // obtain token
        result = AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isOk())
                .andReturn();

        JwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), JwtResponse.class);
        String token = response.getJwtToken();

        AuthenticationTestUtil.verifyAuthentication(mockMvc, token, validUsername);
    }

    @Test
    public void testTraditionalAuthUsernameDoesNotExist() throws Exception {
        JwtRequest request = new JwtRequest(nonExistentUsername, validPassword);
        String requestBody = mapper.writeValueAsString(request);

        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")))
                .andReturn();
    }

    @Test
    public void testTraditionalAuthIncorrectPassword() throws Exception {
        JwtRequest request = new JwtRequest(validUsername, badPassword);
        String requestBody = mapper.writeValueAsString(request);

        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")))
                .andReturn();
    }
}

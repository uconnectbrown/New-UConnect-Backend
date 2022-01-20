package integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.UConnectBackendApplication;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.OAuthJwtResponse;
import com.uconnect.backend.security.oauth.OAuthRequest;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import com.uconnect.backend.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UConnectBackendApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GoogleOAuthTest extends BaseIntTest {
    // unfortunately we have to mock the Google sign in flow, since obtaining the one-time authorization code from
    // Google is almost impossible to automate as of now (1/10/2022)
    @MockBean
    private OidcAuthorizationCodeAuthenticationProvider oidcAuthorizationCodeAuthenticationProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private String userTableName;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mock
    private DefaultOidcUser validUser;

    @Mock
    private Authentication successAuth;

    private final String validOAuthUsername = "tester@brown.edu";
    private final OAuthRequest oAuthRequest = OAuthRequest.builder().authCode("random crap").build();
    private String oAuthRequestString;
    private String registrationId;

    @BeforeEach
    public void setup() throws InterruptedException, JsonProcessingException {
        oAuthRequestString = mapper.writeValueAsString(oAuthRequest);
        registrationId = "google";

        if (ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class)) {
            // wait for the new table to become available
            Thread.sleep(10);
        }
    }

    @Test
    @Order(1)
    public void testFirstTimeOAuthSuccess() throws Exception {
        // make sure there is no entry of this user yet
        assertThrows(UserNotFoundException.class, () -> ddbAdapter.findByUsername(validOAuthUsername));

        when(oidcAuthorizationCodeAuthenticationProvider.supports(OAuth2LoginAuthenticationToken.class)).thenReturn(true);
        when(oidcAuthorizationCodeAuthenticationProvider.authenticate(any(Authentication.class)))
                .thenReturn(successAuth);
        when(successAuth.getPrincipal()).thenReturn(validUser);
        when(validUser.getEmail()).thenReturn(validOAuthUsername);

        MvcResult result;

        result = AuthenticationTestUtil.loginOAuth(mockMvc, registrationId, oAuthRequestString)
                .andExpect(status().isOk())
                .andReturn();

        OAuthJwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), OAuthJwtResponse.class);
        String token = response.getJwtToken();
        String returnedUsername = response.getUsername();

        assertEquals(validOAuthUsername, returnedUsername);
        AuthenticationTestUtil.verifyAuthentication(mockMvc, token, returnedUsername);

        User user = ddbAdapter.findByUsername(validOAuthUsername);
        assertEquals(UserCreationType.O_AUTH, user.getCreationType());
        assertEquals(validOAuthUsername, user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @Order(2)
    public void testSecondTimeOAuthSuccess() throws Exception {
        UserService userServiceSpy = spy(userService);

        // make sure this user still exists
        ddbAdapter.findByUsername(validOAuthUsername);

        when(oidcAuthorizationCodeAuthenticationProvider.supports(OAuth2LoginAuthenticationToken.class)).thenReturn(true);
        when(oidcAuthorizationCodeAuthenticationProvider.authenticate(any(Authentication.class)))
                .thenReturn(successAuth);
        when(successAuth.getPrincipal()).thenReturn(validUser);
        when(validUser.getEmail()).thenReturn(validOAuthUsername);

        MvcResult result;

        result = AuthenticationTestUtil.loginOAuth(mockMvc, registrationId, oAuthRequestString)
                .andExpect(status().isOk())
                .andReturn();

        // make sure no new record was created when the same user logs in again
        verify(userServiceSpy, times(0)).createNewUser(any(User.class));

        OAuthJwtResponse response = mapper.readValue(result.getResponse().getContentAsString(), OAuthJwtResponse.class);
        String token = response.getJwtToken();
        String returnedUsername = response.getUsername();

        assertEquals(validOAuthUsername, returnedUsername);
        AuthenticationTestUtil.verifyAuthentication(mockMvc, token, returnedUsername);

        User user = ddbAdapter.findByUsername(validOAuthUsername);
        assertEquals(UserCreationType.O_AUTH, user.getCreationType());
        assertEquals(validOAuthUsername, user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    @Order(3)
    public void testUnknownRegistration() throws Exception {
        List<String> invalidRegIds = Arrays.asList("github", "azure", "pornhub");

        for (String invalidRegId : invalidRegIds) {
            AuthenticationTestUtil.loginOAuth(mockMvc, invalidRegId, oAuthRequestString)
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(
                            containsString("Provided registration ID is unknown or not yet supported")));
        }
    }

    @Test
    @Order(4)
    public void testTraditionalLoginOAuthCreatedAccount() throws Exception {
        // make sure this user was created through OAuth
        User user = ddbAdapter.findByUsername(validOAuthUsername);
        assertEquals(UserCreationType.O_AUTH, user.getCreationType());
        assertEquals(validOAuthUsername, user.getUsername());
        assertNull(user.getPassword());

        JwtRequest request = new JwtRequest(validOAuthUsername, "");
        String requestBody = mapper.writeValueAsString(request);

        // fail with empty password
        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        containsString("Password length must be between 8 and 32 characters (inclusive)")));

        // fail with random password
        request.setPassword("0u9gqa3wrhklnju_hrlk;JKTAWE1-0");
        requestBody = mapper.writeValueAsString(request);
        AuthenticationTestUtil.loginTraditional(mockMvc, requestBody)
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Invalid credentials / Account disabled / Account locked")));
    }

    @Test
    @Order(5)
    public void testOAuthLoginTraditionalCreatedAccount() throws Exception {
        // set up spies
        UserService userServiceSpy = spy(userService);
        UserDAO userDAOSpy = spy(userDAO);

        // set up mocks
        String traditionalUsername = "goodCatholicWife@brown.edu";
        String password = "12345678";
        when(oidcAuthorizationCodeAuthenticationProvider.supports(OAuth2LoginAuthenticationToken.class)).thenReturn(true);
        when(oidcAuthorizationCodeAuthenticationProvider.authenticate(any(Authentication.class)))
                .thenReturn(successAuth);
        when(successAuth.getPrincipal()).thenReturn(validUser);
        when(validUser.getEmail()).thenReturn(traditionalUsername);

        User user = User.builder()
                .username(traditionalUsername)
                .password(password)
                .firstName(traditionalUsername)
                .lastName(traditionalUsername)
                .classYear("2021")
                .build();
        // register user with username + pw
        String requestBody = mapper.writeValueAsString(user);
        AuthenticationTestUtil.createUserTraditional(mockMvc, requestBody)
                .andExpect(status().isAccepted())
                .andReturn();

        // make sure this user was created through traditional
        user = ddbAdapter.findByUsername(traditionalUsername);
        assertEquals(UserCreationType.TRADITIONAL, user.getCreationType());
        assertEquals(traditionalUsername, user.getUsername());
        assertTrue(passwordEncoder.matches(password, user.getPassword()));

        // attempt to log in through OAuth
        AuthenticationTestUtil.loginOAuth(mockMvc, registrationId, oAuthRequestString)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(
                        String.format("Requested user was not created through creation type: %s",
                                UserCreationType.O_AUTH.name()))));

        // make sure the existing user record was not modified in any way
        verify(userServiceSpy, times(0)).createNewUser(any(User.class));
        verify(userDAOSpy, times(0)).saveUser(any(User.class));
        assertEquals(user, ddbAdapter.findByUsername(traditionalUsername));
    }
}

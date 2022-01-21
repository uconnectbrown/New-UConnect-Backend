package integration;

import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.user.model.EmailVerification;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmailVerificationTest extends BaseIntTest {

    private static User validUser;

    private static boolean init = true;

    @BeforeEach
    public void setup() {
        if (init) {
            // Populate test data
            setupDdb();

            init = false;
        }
        validUser = MockData.generateValidUser();
    }

    @Test
    public void testVerificationSuccess() throws Exception {
        AuthenticationTestUtil.createUserTraditionalSuccess(mockMvc, validUser);
        assertEquals("notVerified", UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, false,
                ddbAdapter, userTableName));

        // simulating email retrieval is too complicated, hack the db for verification code
        EmailVerification codeModel = EmailVerification
                .builder()
                .emailAddress(validUser.getUsername())
                .build();
        codeModel = ddbAdapter
                .query(emailVerificationTableName, codeModel, EmailVerification.class)
                .get(0);

        UserTestUtil.verifyUser(mockMvc, codeModel)
                .andExpect(status().isAccepted());
        // make sure entry is deleted
        assertEquals(0, ddbAdapter.query(emailVerificationTableName, codeModel, EmailVerification.class).size());

        // attempt to log in again, now the user should be verified
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, false,
                ddbAdapter, userTableName);
        AuthenticationTestUtil.verifyAuthenticationSuccess(mockMvc, token, validUser.getUsername());
    }

    @Test
    public void testFailureNoVerificationInProgress() throws Exception {
        EmailVerification codeModel = EmailVerification
                .builder()
                .emailAddress(validUser.getUsername())
                .verificationCode("antimatter")
                .build();

        // make sure entry does not exist
        assertEquals(0, ddbAdapter.query(emailVerificationTableName, codeModel, EmailVerification.class).size());

        UserTestUtil.verifyUser(mockMvc, codeModel)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(
                        "Verification failed. Please contact us if this happens repeatedly")));
    }

    @Test
    public void testFailureIncorrectCode() throws Exception {
        testFailure("definitely not it");
    }

    @Test
    public void testFailureNullCodeProvided() throws Exception {
        testFailure(null);
    }

    @Test
    public void testFailureEmptyCodeProvided() throws Exception {
        testFailure("");
    }

    private void testFailure(String code) throws Exception {
        AuthenticationTestUtil.createUserTraditionalSuccess(mockMvc, validUser);
        assertEquals("notVerified", UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, false,
                ddbAdapter, userTableName));

        EmailVerification codeModel = EmailVerification
                .builder()
                .emailAddress(validUser.getUsername())
                .verificationCode(code)
                .build();

        // make sure entry exists
        assertEquals(1, ddbAdapter.query(emailVerificationTableName, codeModel, EmailVerification.class).size());

        UserTestUtil.verifyUser(mockMvc, codeModel)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString(
                        "Verification failed. Please contact us if this happens repeatedly")));

        // make sure entry still exists
        assertEquals(1, ddbAdapter.query(emailVerificationTableName, codeModel, EmailVerification.class).size());

        // attempt to log in again, nothing should change
        assertEquals("notVerified", UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, false,
                ddbAdapter, userTableName));
    }
}

package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.user.model.Location;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateUserTest extends BaseIntTest {
    @Autowired
    private ObjectMapper mapper;

    private static User oldUser;

    private static boolean init = true;

    @BeforeEach
    public void setup() {
        if (init) {
            setupDdb();

            init = false;
        }
        oldUser = MockData.generateValidUser();
        oldUser.setCreationType(UserCreationType.TRADITIONAL);
        oldUser.setVerified(true);
    }

    @Test
    public void testSuccessUpdateNames() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        assertEquals(oldUser.getFirstName(), savedUser.getFirstName());
        assertEquals(oldUser.getLastName(), savedUser.getLastName());

        setAutoGenFields();
        String newFirstName = "Jeff";
        String newLastName = "Salmon";

        User newRecord = User.builder()
                .username(username)
                .firstName(newFirstName)
                .lastName(newLastName)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        assertEquals(newFirstName, newUser.getFirstName());
        assertEquals(newLastName, newUser.getLastName());
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testSuccessUpdatePassword() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        setAutoGenFields();
        String newPassword = "omicronHashOhGod*7^&";

        User newRecord = User.builder()
                .username(username)
                .password(newPassword)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        verifyUnchangedProperties(oldUser, newUser);

        AuthenticationTestUtil.loginTraditional(mockMvc, new JwtRequest(oldUser.getUsername(), oldUser.getPassword()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Invalid credentials / Account disabled / Account locked"));
        token = UserTestUtil.getTokenForTraditionalUser(mockMvc, newRecord, false, ddbAdapter, userTableName);
        AuthenticationTestUtil.verifyAuthenticationSuccess(mockMvc, token, username);
    }

    @Test
    public void testSuccessUpdateObject() throws Exception {
        // location
        Location oldLocation = Location.builder()
                .city("Old City")
                .state("Casa Blanca")
                .country("Nexico")
                .build();
        oldUser.setLocation(oldLocation);
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        Location savedLocation = ddbAdapter.findByUsername(username).getLocation();
        assertEquals(oldUser.getLocation().getCity(), savedLocation.getCity());
        assertEquals(oldUser.getLocation().getState(), savedLocation.getState());
        assertEquals(oldUser.getLocation().getCountry(), savedLocation.getCountry());

        setAutoGenFields();
        Location newLocation = Location.builder()
                .city("New City")
                .state("Mama Caliente")
                .country("Bexico")
                .build();

        User newRecord = User.builder()
                .username(username)
                .location(newLocation)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        Location returnedLocation = newUser.getLocation();
        assertEquals(newLocation.getCity(), returnedLocation.getCity());
        assertEquals(newLocation.getState(), returnedLocation.getState());
        assertEquals(newLocation.getCountry(), returnedLocation.getCountry());
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testSuccessUpdateStringList() {

    }

    @Test
    public void testSuccessUpdateObjectList() {

    }

    @Test
    public void testSuccessUpdateStringSet() {

    }

    @Test
    public void testSuccessUpdateObjectSet() {
        // courses
    }

    @Test
    public void testFailureUpdateOAuthPassword() {

    }

    @Test
    public void testFailureNullUsername() {

    }

    @Test
    public void testFailureUpdateForbiddenFields() {

    }

    @Test
    public void testFailureUnauthorizedUpdate() {

    }

    private User getUpdatedUser(User newRecord, String token) throws Exception {
        String username = newRecord.getUsername();
        UserTestUtil.updateUser(mockMvc, username, newRecord, token)
                .andExpect(status().isOk())
                .andExpect(content().string(String.format("Successfully updated user %s", username)))
                .andReturn();

        return UserTestUtil.getUserModel(mockMvc, username, username, token);
    }

    private void setAutoGenFields() throws UserNotFoundException {
        User fromDb = ddbAdapter.findByUsername(oldUser.getUsername());
        assertNotNull(fromDb.getId());
        assertNotNull(fromDb.getCreatedAt());
        oldUser.setId(fromDb.getId());
        oldUser.setCreatedAt(fromDb.getCreatedAt());
    }

    private void verifyUnchangedProperties(User expected, User actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAuthorities(), actual.getAuthorities());
        assertEquals(expected.isVerified(), actual.isVerified());
        assertEquals(expected.getCreationType(), actual.getCreationType());
        assertEquals(expected.isProfileCompleted(), actual.isProfileCompleted());
        assertEquals(expected.getCreatedAt(), actual.getCreatedAt());
    }
}

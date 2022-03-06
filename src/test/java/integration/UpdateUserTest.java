package integration;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.security.authority.UserAuthority;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.user.model.Course;
import com.uconnect.backend.user.model.InterestItem;
import com.uconnect.backend.user.model.Location;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UpdateUserTest extends BaseIntTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    private static User oldUser;

    private static User simulatedOAuthUser;
    private static String simulatedOAuthUserToken;

    private static boolean init = true;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();

            simulatedOAuthUser = User.builder()
                    .username("coon@brown.edu")
                    .password("coon_and_friends")
                    .creationType(UserCreationType.TRADITIONAL)
                    .verified(true)
                    .profileCompleted(false)
                    .createdAt(new Date())
                    .build();
            simulatedOAuthUserToken = UserTestUtil.getTokenForTraditionalUser(mockMvc, simulatedOAuthUser, true, ddbAdapter, userTableName);
            simulatedOAuthUser.setId(UserTestUtil.getUserModel(mockMvc, simulatedOAuthUser.getUsername(), simulatedOAuthUser.getUsername(),
                    simulatedOAuthUserToken).getId());

            init = false;
        }
        oldUser = MockData.generateValidUser();
        oldUser.setCreationType(UserCreationType.TRADITIONAL);
        oldUser.setVerified(true);
    }

    @AfterEach
    public void cleanup() {
        ddbAdapter.save(userTableName, simulatedOAuthUser);
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
    public void testSuccessUpdateStringList() throws Exception {
        String major1 = "Jarritos Studies";
        String major2 = "Social Engineering";
        oldUser.setMajors(ImmutableList.of(major1, major2));
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        assertEquals(major1, savedUser.getMajors().get(0));
        assertEquals(major2, savedUser.getMajors().get(1));

        setAutoGenFields();

        String major3 = "Syrup Management";
        String major4 = "Communist Acquisition";
        User newRecord = User.builder()
                .username(username)
                .majors(ImmutableList.of(major3, major4))
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        assertEquals(major3, newUser.getMajors().get(0));
        assertEquals(major4, newUser.getMajors().get(1));
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testSuccessUpdateObjectList() throws Exception {
        List<InterestItem> interest1 = ImmutableList.of(
                new InterestItem(1, "Interest 1"),
                new InterestItem(2, "Interest 2"),
                new InterestItem(3, "Interest 3")
        );
        oldUser.setInterests1(interest1);
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        for (int i = 0; i < 3; i++) {
            assertEquals(interest1.get(i), savedUser.getInterests1().get(i));
        }

        setAutoGenFields();

        List<InterestItem> newInterest1 = ImmutableList.of(
                new InterestItem(4, "Interest 4"),
                new InterestItem(5, "Interest 52"),
                new InterestItem(97, "Interest 23")
        );
        User newRecord = User.builder()
                .username(username)
                .interests1(newInterest1)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        for (int i = 0; i < 3; i++) {
            assertEquals(newInterest1.get(i), newUser.getInterests1().get(i));
        }
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testSuccessUpdateStringSet() throws Exception {
        Set<String> sports = ImmutableSet.of("Ice Hockey", "Snow Hockey", "Liquid-Hockey");
        oldUser.setVarsitySports(sports);
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        Set<String> savedSports = savedUser.getVarsitySports();
        for (String sport : sports) {
            assertTrue(savedSports.remove(sport));
        }
        assertTrue(savedSports.isEmpty());

        setAutoGenFields();

        Set<String> newSports = ImmutableSet.of("Wind Hockey", "Fire Hockey", "WWE Hockey");
        User newRecord = User.builder()
                .username(username)
                .varsitySports(newSports)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        Set<String> returnedSports = newUser.getVarsitySports();
        for (String sport : newSports) {
            assertTrue(returnedSports.remove(sport));
        }
        assertTrue(returnedSports.isEmpty());
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testSuccessUpdateObjectSet() throws Exception {
        Course.CourseBuilder builder = Course.builder();
        Set<Course> courses = ImmutableSet.of(
                builder.code("APMA 1930V").color("#ffffff")
                        .name("Randomized Algorithms for Counting, Integration and Optimization").build(),
                builder.code("PHIL 1576").color("#ffffff").name("Pornography").build(),
                builder.code("CHIN 6699").color("#ffffff").name("Mao Zedong in 21st Century Ghana").build()
        );
        oldUser.setCourses(courses);
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        Set<Course> savedCourses = savedUser.getCourses();
        for (Course course : courses) {
            assertTrue(savedCourses.remove(course));
        }
        assertTrue(savedCourses.isEmpty());

        setAutoGenFields();

        Set<Course> newCourses = ImmutableSet.of(
                builder.code("PARK 1984").color("#ffffff")
                        // don't get mad at me for this one, blame Matt Stone and Trey Parker
                        .name("Paris Hilton and Stupid Spoiled Whores").build(),
                builder.code("PHIL 1576").color("#ffffff").name("Pornography").build(),
                builder.code("CHIN 6699").color("#ffffff").name("Mao Zedong in 21st Century Ghana").build()
        );
        User newRecord = User.builder()
                .username(username)
                .courses(newCourses)
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        Set<Course> returnedCourses = newUser.getCourses();
        for (Course course : newCourses) {
            assertTrue(returnedCourses.remove(course));
        }
        assertTrue(returnedCourses.isEmpty());
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testFailureUpdateOAuthPassword() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        User savedUser = ddbAdapter.findByUsername(username);
        savedUser.setCreationType(UserCreationType.O_AUTH);
        ddbAdapter.save(userTableName, savedUser);
        oldUser.setCreationType(UserCreationType.O_AUTH);
        setAutoGenFields();
        String newPassword = "omicronHashOhGod*7^&";

        User newRecord = User.builder()
                .username(username)
                .password(newPassword)
                .build();

        getUpdatedUser(newRecord, token);
        User newUser = ddbAdapter.findByUsername(username);
        // 1. should be null in prod, but default mapper save behavior doesn't save null value so we'll pretend this is
        // actually null (as long as updateUser didn't change the old password value)
        // 2. have to directly fetch from db otherwise getUser api erases returned password
        assertTrue(passwordEncoder.matches(oldUser.getPassword(), newUser.getPassword()));
        verifyUnchangedProperties(oldUser, newUser);

        AuthenticationTestUtil.loginTraditional(mockMvc, new JwtRequest(oldUser.getUsername(), oldUser.getPassword()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "Requested user was not created through creation type: TRADITIONAL"));
    }

    @Test
    public void testFailureNullUsername() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        setAutoGenFields();

        User newRecord = User.builder()
                .username(null)
                .build();

        UserTestUtil.updateUser(mockMvc, username, newRecord, token)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "{\"username\":\"User object username cannot be null or empty\"}"))
                .andReturn();
        User newUser = ddbAdapter.findByUsername(username);
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testFailureUpdateId() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        setAutoGenFields();

        User newRecord = User.builder()
                .username(username)
                .id("whateva, whateva, I'll do what I want!")
                .build();

        UserTestUtil.updateUser(mockMvc, username, newRecord, token)
                .andExpect(status().isBadRequest())
                .andExpect(content().string(
                        "{\"id\":\"New users are assigned random IDs\"}"))
                .andReturn();
        User newUser = ddbAdapter.findByUsername(username);
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testFailureUpdateForbiddenFields() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        setAutoGenFields();
        User newRecord = User.builder()
                .username(username)
                .authorities(ImmutableList.of(new UserAuthority("ADMIN")))
                .verified(!oldUser.isVerified())
                .creationType(UserCreationType.O_AUTH)
                .profileCompleted(!oldUser.isProfileCompleted())
                .createdAt(new Date())
                .build();

        User newUser = getUpdatedUser(newRecord, token);
        verifyUnchangedProperties(oldUser, newUser);
    }

    @Test
    public void testFailureUnauthorizedUpdate() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, oldUser, true, ddbAdapter, userTableName);
        String username = oldUser.getUsername();

        setAutoGenFields();

        User newRecord = User.builder()
                .username("somebody@else.username")
                .build();

        UserTestUtil.updateUser(mockMvc, username, newRecord, token)
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(
                        "You are not authorized to make that request. We've got our eyes on you!"))
                .andReturn();
        User newUser = ddbAdapter.findByUsername(username);
        verifyUnchangedProperties(oldUser, newUser);
    }

    // --------------------------
    // - complete profile tests -
    // --------------------------
    @Test
    @SneakyThrows
    public void testSuccessCompleteProfile() {
        Assertions.assertFalse(UserTestUtil.getUserModel(mockMvc, simulatedOAuthUser.getUsername(), simulatedOAuthUser.getUsername(),
                simulatedOAuthUserToken).isProfileCompleted());

        List<InterestItem> fakeInterests = ImmutableList.of(new InterestItem(), new InterestItem(), new InterestItem());
        User completedUser = User.builder()
                .username(simulatedOAuthUser.getUsername())
                .firstName("Emma")
                .lastName("Watson")
                .classYear("2014")
                .majors(ImmutableList.of("Dark Magic"))
                .interests1(fakeInterests)
                .interests2(fakeInterests)
                .interests3(fakeInterests)
                .build();

        Assertions.assertTrue(getUpdatedUser(completedUser, simulatedOAuthUserToken).isProfileCompleted());
    }

    @Test
    @SneakyThrows
    public void testSuccessCompletedProfileStaysComplete() {
        testSuccessCompleteProfile();

        User completedUser = User.builder()
                .username(simulatedOAuthUser.getUsername())
                .majors(ImmutableList.of("Gender Studies"))
                .build();

        Assertions.assertTrue(getUpdatedUser(completedUser, simulatedOAuthUserToken).isProfileCompleted());
    }

    @Test
    @SneakyThrows
    public void testFailureCompleteProfileNoMajor() {
        Assertions.assertFalse(UserTestUtil.getUserModel(mockMvc, simulatedOAuthUser.getUsername(), simulatedOAuthUser.getUsername(),
                simulatedOAuthUserToken).isProfileCompleted());

        List<InterestItem> fakeInterests = ImmutableList.of(new InterestItem(), new InterestItem(), new InterestItem());
        User completedUser = User.builder()
                .username(simulatedOAuthUser.getUsername())
                .firstName("Emma")
                .lastName("Watson")
                .classYear("2014")
                .majors(ImmutableList.of())
                .interests1(fakeInterests)
                .interests2(fakeInterests)
                .interests3(fakeInterests)
                .build();

        Assertions.assertFalse(getUpdatedUser(completedUser, simulatedOAuthUserToken).isProfileCompleted());
    }

    // -----------
    // - helpers -
    // -----------
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

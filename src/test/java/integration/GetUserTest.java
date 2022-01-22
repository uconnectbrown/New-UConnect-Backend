package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.user.model.Course;
import com.uconnect.backend.user.model.InterestItem;
import com.uconnect.backend.user.model.Location;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
public class GetUserTest extends BaseIntTest {

    @Autowired
    private ObjectMapper mapper;

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
    public void testUserExists() throws Exception {
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

        String response = UserTestUtil.getUser(mockMvc, validUser.getUsername(), validUser.getUsername(), token)
                .andReturn().getResponse().getContentAsString();

        User returnedUser = mapper.readValue(response, User.class);
        // make sure password is taken out
        assertNotEquals(validUser.getPassword(), returnedUser.getPassword());
        assertEquals(validUser.getUsername(), returnedUser.getUsername());
        assertEquals(validUser.getFirstName(), returnedUser.getFirstName());
        assertEquals(validUser.getLastName(), returnedUser.getLastName());
    }

    @Test
    public void testUserNotFound() throws Exception {
        String invalidUsername = "no@exist.org";

        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);
        UserTestUtil.getUser(mockMvc, validUser.getUsername(), invalidUsername, token)
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Requested user either does not exist or is not available at this time")));
    }

    @Test
    public void testInterestsConversion() throws Exception {
        InterestItem.InterestItemBuilder builder = InterestItem.builder();
        List<InterestItem> interest1 = ImmutableList.of(
                builder.interest("Data Science").index(8).build(),
                builder.interest("Education").index(9).build(),
                builder.interest("Math and Statistics").index(21).build());
        validUser.setInterests1(interest1);

        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

        // check in db
        User foundUser = ddbAdapter.findByUsername(validUser.getUsername());
        for (int i = 0; i < interest1.size(); i++) {
            assertEquals(interest1.get(i), foundUser.getInterests1().get(i));
        }

        // check data after being fetched from db
        String response = UserTestUtil.getUser(mockMvc, validUser.getUsername(), validUser.getUsername(), token)
                .andReturn().getResponse().getContentAsString();
        foundUser = mapper.readValue(response, User.class);
        for (int i = 0; i < interest1.size(); i++) {
            assertEquals(interest1.get(i), foundUser.getInterests1().get(i));
        }
    }

    @Test
    public void testLocationConversion() throws Exception {
        validUser.setLocation(Location.builder()
                .country("Finland")
                .state("Naramiuyyd")
                .city("Kuchi")
                .build());

        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

        // check in db
        User foundUser = ddbAdapter.findByUsername(validUser.getUsername());
        assertEquals(validUser.getLocation(), foundUser.getLocation());

        // check data after being fetched from db
        String response = UserTestUtil.getUser(mockMvc, validUser.getUsername(), validUser.getUsername(), token)
                .andReturn().getResponse().getContentAsString();
        foundUser = mapper.readValue(response, User.class);
        assertEquals(validUser.getLocation(), foundUser.getLocation());
    }

    @Test
    public void testCoursesConversion() throws Exception {
        Course.CourseBuilder builder = Course.builder();
        Set<Course> courses = ImmutableSet.of(
                builder.code("APMA 1930V").color("#ffffff")
                        .name("Randomized Algorithms for Counting, Integration and Optimization").build(),
                builder.code("CLPS 0010").color("#ffffff").name("Mind, Brain and Behavior").build(),
                builder.code("CHIN 6699").color("#ffffff").name("Mao Zedong in 21st Century Ghana").build()
        );
        validUser.setCourses(courses);

        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

        // check in db
        User foundUser = ddbAdapter.findByUsername(validUser.getUsername());
        for (Course course : courses) {
            assertTrue(foundUser.getCourses().remove(course));
        }
        assertTrue(foundUser.getCourses().isEmpty());

        // check data after being fetched from db
        String response = UserTestUtil.getUser(mockMvc, validUser.getUsername(), validUser.getUsername(), token)
                .andReturn().getResponse().getContentAsString();
        foundUser = mapper.readValue(response, User.class);
        for (Course course : courses) {
            assertTrue(foundUser.getCourses().remove(course));
        }
        assertTrue(foundUser.getCourses().isEmpty());
    }
}

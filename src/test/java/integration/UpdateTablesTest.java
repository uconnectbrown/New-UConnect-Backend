package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.search.model.Concentration;
import com.uconnect.backend.user.model.Course;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UpdateTablesTest extends BaseIntTest {

    @Autowired
    private ObjectMapper mapper;

    private static boolean init = true;

    private static User validUser = MockData.generateValidUser();

    private static String token;

    private static Course course1 =
            Course.builder().name("Yay Testing").build();

    private static Course course2 =
            Course.builder().name("Can you tell I like testing").build();

    private static Concentration concentration1 =
            Concentration.builder().name("Concentrating...").build();

    private static Concentration concentration2 =
            Concentration.builder().name("hihihi").build();

    private static Set<Course> courses =
            new HashSet<>(Arrays.asList(course1, course2));

    private static List<String> concentrations =
            new ArrayList<>(Arrays.asList(concentration1.getName(),
                    concentration2.getName()));

    @BeforeEach
    private void setup() throws Exception {
        if (init) {
            setupDdb();

            validUser.setCourses(courses);
            validUser.setMajors(concentrations);
            createNewUserTraditional(validUser);
            UserTestUtil.verifyUserHack(ddbAdapter, validUser.getUsername(),
                    userTableName);
            token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser,
                    false, ddbAdapter, userTableName);

            init = false;
        }
    }

    private void createNewUserTraditional(User user) throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders
                        .post("/v1/user/signup/createNewUserTraditional")
                        .content(mapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isAccepted());
    }

    private Set<String> getStudentsByConcentration(String name,
            ResultMatcher status) throws Exception {
        String json = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format(
                                "/v1/search/concentration/%s",
                                name))
                        .header("Authorization",
                                String.format("Bearer %s", token))
                        .header("Username", validUser.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(json, new TypeReference<Set<String>>() {});
    }

    private Set<String> getStudentsByCourse(String name, ResultMatcher status)
            throws Exception {
        String json = mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format(
                                "/v1/search/course/%s",
                                name))
                        .header("Authorization",
                                String.format("Bearer %s", token))
                        .header("Username", validUser.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readValue(json, new TypeReference<Set<String>>() {});
    }

    @Test
    @Order(1)
    public void testAddToConcentrationsOnSignup() throws Exception {
        Set<String> studentsInConcentration1 = getStudentsByConcentration(
                concentration1.getName(), status().isOk());
        Set<String> studentsInConcentration2 = getStudentsByConcentration(
                concentration2.getName(), status().isOk());
        assertEquals(studentsInConcentration1.size(), 1);
        assertEquals(studentsInConcentration1.iterator().next(), validUser.getUsername());
        assertEquals(studentsInConcentration2.size(), 1);
        assertEquals(studentsInConcentration2.iterator().next(), validUser.getUsername());
    }

    @Test
    @Order(2)
    public void testAddToCoursesOnSignup() throws Exception {
        Set<String> studentsInCourse1 = getStudentsByCourse(course1.getName(), status().isOk());
        Set<String> studentsInCourse2 = getStudentsByCourse(course2.getName(), status().isOk());
        assertEquals(studentsInCourse1.size(), 1);
        assertEquals(studentsInCourse1.iterator().next(), validUser.getUsername());
        assertEquals(studentsInCourse2.size(), 1);
        assertEquals(studentsInCourse2.iterator().next(), validUser.getUsername());
    }
}

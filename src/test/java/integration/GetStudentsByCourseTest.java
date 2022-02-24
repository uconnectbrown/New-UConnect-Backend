package integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.search.model.CourseRoster;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class GetStudentsByCourseTest extends BaseIntTest {

    private ObjectMapper mapper = new ObjectMapper();

    private CourseRoster validCourse = new CourseRoster();

    private Set<String> students = new HashSet<>();

    private User validUser = MockData.generateValidUser();

    private static boolean init = true;

    @BeforeEach
    public void setup() {
        if (init) {
            setupDdb();
            init = false;
        }
        for (int i = 0; i < 10; i++) {
            students.add(MockData.generateValidUser().getUsername());
        }
        validCourse.setName("testCourse");
        validCourse.setStudents(students);
        ddbAdapter.save(courseTableName, validCourse);
    }

    private MvcResult testGetStudents(String course, ResultMatcher status, Set<String> students)
            throws Exception {
        ResultMatcher content = students == null
                ? content().string("")
                : content().json(mapper.writeValueAsString(students));
        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format("/v1/search/getStudentsByCourse/%s", course))
                        .header("Authorization", String.format("Bearer %s", token))
                        .header("Username", validUser.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andExpect(content)
                .andReturn();
    }

    @Test
    public void testValid() throws Exception {
        testGetStudents(validCourse.getName(), status().isOk(), students);
    }

    @Test
    public void testCourseNotFound() throws Exception {
        ddbAdapter.delete(courseTableName, validCourse);
        testGetStudents(validCourse.getName(), status().isNotFound(), null);
    }
}

package integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.AuthenticationTestUtil;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class GetStudentsByNameTest extends BaseIntTest {

    @Autowired
    private ObjectMapper mapper;

    private static Set<User> students = new HashSet<>();

    private static User validUser;

    private static String token;

    private static boolean init = true;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();

            for (int i = 0; i < 100; i++) {
                User u = MockData.generateValidUser();
                students.add(u);
                ddbAdapter.save(userTableName, u);
            }
            validUser = MockData.generateValidUser();
            AuthenticationTestUtil.createUserTraditional(mockMvc, validUser);
            UserTestUtil.verifyUserHack(ddbAdapter, validUser.getUsername(),
                    userTableName);
            students.add(ddbAdapter.findByUsername(validUser.getUsername()));
            token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser,
                    false, ddbAdapter, userTableName);

            init = false;
        }
    }

    private MvcResult testGetStudents(String name,
            ResultMatcher status,
            Object expected)
            throws Exception {
        ResultMatcher content;
        if (expected instanceof Set<?>) {
            content = content().json(mapper.writeValueAsString(expected));
        } else if (expected instanceof String) {
            content = content().string((String) expected);
        } else if (expected == null) {
            content = content().string("");
        } else {
            throw new IllegalArgumentException();
        }

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format(
                                "/v1/search/name/%s",
                                name))
                        .header("Authorization",
                                String.format("Bearer %s", token))
                        .header("Username", validUser.getUsername())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status)
                .andExpect(content)
                .andReturn();
    }

    @Test
    public void testLowercaseQueries() throws Exception {
        for (int i = 0; i < 26; i++) {
            Set<User> expected = new HashSet<>();
            char bucket = (char)('a' + i);
            for (User u : students) {
                if (u.getFirstNameBucket().equals(bucket)
                        || u.getLastNameBucket().equals(bucket)) {
                    expected.add(u);
                }
            }
            testGetStudents(bucket + "", status().isOk(), expected);
        }
    }

    @Test
    public void testUppercaseQueries() throws Exception {
        for (int i = 0; i < 26; i++) {
            Set<User> expected = new HashSet<>();
            char bucket = (char)('A' + i);
            for (User u : students) {
                if (u.getFirstNameBucket().equals(Character.toLowerCase(bucket))
                        || u.getLastNameBucket().equals(Character.toLowerCase(bucket))) {
                    expected.add(u);
                }
            }
            testGetStudents(bucket + "", status().isOk(), expected);
        }
    }
}

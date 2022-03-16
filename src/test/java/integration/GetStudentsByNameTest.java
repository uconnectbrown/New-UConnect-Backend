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
import org.junit.jupiter.api.AfterEach;
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

    private Set<User> students = new HashSet<>();

    private User validUser;

    private String validQuery;

    private static boolean init = true;

    @BeforeEach
    public void setup() throws Exception {
        if (init) {
            setupDdb();
            init = false;
        }
        for (int i = 0; i < 100; i++) {
            User u = MockData.generateValidUser();
            students.add(u);
            ddbAdapter.save(userTableName, u);
        }
        validUser = MockData.generateValidUser();
        validQuery = validUser.getFirstName();
        AuthenticationTestUtil.createUserTraditional(mockMvc, validUser);
        UserTestUtil.verifyUserHack(ddbAdapter, validUser.getUsername(),
                userTableName);
        students.add(ddbAdapter.findByUsername(validUser.getUsername()));
    }

    @AfterEach
    public void teardown() {
        for (User u : students) {
            ddbAdapter.delete(userTableName, u);
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
        String token =
                UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser,
                        false, ddbAdapter, userTableName);

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
    public void testValid() throws Exception {
        Set<User> expected = new HashSet<>();
        Character bucket = Character.toLowerCase(validQuery.charAt(0));
        for (User u : students) {
            if (u.getFirstNameBucket().equals(bucket)
                    || u.getLastNameBucket().equals(bucket)) {
                expected.add(u);
            }
        }
        testGetStudents(validQuery, status().isOk(), expected);
    }

    @Test
    public void testCaseInsensitive() throws Exception {
        Set<User> expected = new HashSet<>();
        Character bucket = Character.toLowerCase(validQuery.charAt(0));
        // make first letter of query uppercase
        validQuery = new StringBuilder(validQuery).replace(0, 1, "" + bucket).toString();
        for (User u : students) {
            if (u.getFirstNameBucket().equals(bucket) || u.getLastNameBucket().equals(bucket)) {
                expected.add(u);
            }
        }
        testGetStudents(validQuery, status().isOk(), expected);
    }
}

package integration;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.search.model.Concentration;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class GetStudentsByConcentrationTest extends BaseIntTest {

    @Autowired
    private ObjectMapper mapper;

    private Concentration validConcentration = new Concentration();

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
        validConcentration.setName("testConcentration");
        validConcentration.setStudents(students);
        ddbAdapter.save(concentrationTableName, validConcentration);
    }

    @AfterEach
    public void teardown() {
        return;
    }

    private MvcResult testGetStudents(String concentration,
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
                        true, ddbAdapter, userTableName);

        return mockMvc
                .perform(MockMvcRequestBuilders
                        .get(String.format(
                                "/v1/search/getStudentsByConcentration/%s",
                                concentration))
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
        testGetStudents(validConcentration.getName(), status().isOk(),
                validConcentration.getStudents());
    }

    @Test
    public void testNotFound() throws Exception {
        testGetStudents("invalidConcentrationName", status().isNotFound(),
                "Could not find concentration: invalidConcentrationName");
    }
}

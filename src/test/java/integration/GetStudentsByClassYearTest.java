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
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

public class GetStudentsByClassYearTest extends BaseIntTest {

  @Autowired
  private ObjectMapper mapper;

  private User validUser = MockData.generateValidUser();

  private String validClassYear = "2023";

  private String otherValidClassYear = "2022";

  private String invalidClassYear = "3";

  private Set<String> studentsInValidClassYear = new HashSet<>();

  private Set<User> students = new HashSet<>();

  private Random rand = new Random();

  private static boolean init = true;
  
  @BeforeEach
  public void setup() {
    if (init) {
      setupDdb();
      init = false;
    }
    
    // Add students with classYear == validClassYear
    for (int i = 0; i < 5; i++) {
      User u = MockData.generateValidUser();
      u.setClassYear(validClassYear);
      studentsInValidClassYear.add(u.getUsername());
      students.add(u);
      ddbAdapter.save(userTableName, u); 
    }

    // Add students with random classYear in [1000, 1999]
    for (int i = 0; i < 5; i++) {
      User u = MockData.generateValidUser();
      u.setClassYear(String.valueOf(1000 + rand.nextInt(1000)));
      students.add(u);
      ddbAdapter.save(userTableName, u);
    }
  }

  @AfterEach
  public void teardown() {
    for (User u : students) {
      ddbAdapter.delete(userTableName, u);
    }
  }

  private MvcResult testGetStudents(String classYear, ResultMatcher status, Object expected)
          throws Exception {
      ResultMatcher content;
      if (expected instanceof Set<?>) {
        content = content().json(mapper.writeValueAsString(expected));
      } else if (expected instanceof String) {
        content = content().string((String)expected);
      } else if (expected == null) {
        content = content().string("");
      } else {
        throw new IllegalArgumentException();
      }
      String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, true, ddbAdapter, userTableName);

      return mockMvc
              .perform(MockMvcRequestBuilders
                      .get(String.format("/v1/search/getStudentsByClassYear/%s", classYear))
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
    testGetStudents(validClassYear, status().isOk(), studentsInValidClassYear);
  }

  @Test
  public void testNotFound() throws Exception {
    testGetStudents(otherValidClassYear, status().isOk(), Collections.emptySet());
  }
  
  @Test
  public void testInvalidClassYear() throws Exception {
    testGetStudents(invalidClassYear, status().isBadRequest(), "Invalid class year provided.");
  }
}

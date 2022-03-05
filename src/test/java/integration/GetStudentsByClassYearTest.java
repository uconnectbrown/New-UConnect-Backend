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

  private static User validUser = MockData.generateValidUser();

  private static final String VALID_INTEGER_CLASS_YEAR = "2023";

  private static final String VALID_DOUBLE_CLASS_YEAR = "2023.5";

  private static final String OTHER_VALID_INTEGER_CLASS_YEAR = "2022";

  private static final String INVALID_INTEGER_CLASS_YEAR = "3";

  private static final String INVALID_DOUBLE_CLASS_YEAR = "1000.3";

  private static final String NAN = "nan";

  private static Set<String> studentsInValidIntegerClassYear = new HashSet<>();

  private static Set<String> studentsInValidDoubleClassYear = new HashSet<>();

  private static Set<User> students = new HashSet<>();

  private static Random rand = new Random();

  private static boolean init = true;
  
  @BeforeEach
  public void setup() {
    if (init) {
      setupDdb();

      // Add students with classYear == validIntegerClassYear
      for (int i = 0; i < 5; i++) {
        User u = MockData.generateValidUser();
        u.setClassYear(VALID_INTEGER_CLASS_YEAR);
        studentsInValidIntegerClassYear.add(u.getUsername());
        students.add(u);
        ddbAdapter.save(userTableName, u); 
      }

      for (int i = 0; i < 5; i++) {
        User u = MockData.generateValidUser();
        u.setClassYear(VALID_DOUBLE_CLASS_YEAR);
        studentsInValidDoubleClassYear.add(u.getUsername());
        students.add(u);
        ddbAdapter.save(userTableName, u);
      }

      // Add students with random classYear in [1000, 1999]
      for (int i = 0; i < 5; i++) {
        User u = MockData.generateValidUser();
        Double randomClassYear = 1000 + rand.nextInt(999) + (rand.nextInt(2) == 1 ? 0.5 : 0);
        if (randomClassYear.equals(Double.valueOf(OTHER_VALID_INTEGER_CLASS_YEAR))) {
          randomClassYear++;
        }
        u.setClassYear(String.valueOf(randomClassYear));
        students.add(u);
        ddbAdapter.save(userTableName, u);
      }

      init = false;
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
                      .get(String.format("/v1/search/class-year/%s", classYear))
                      .header("Authorization", String.format("Bearer %s", token))
                      .header("Username", validUser.getUsername())
                      .contentType(MediaType.APPLICATION_JSON)
                      .accept(MediaType.APPLICATION_JSON))
              .andExpect(status)
              .andExpect(content)
              .andReturn();
  }

  @Test
  public void testIntegerClassYearWithinBounds() throws Exception {
    testGetStudents(VALID_INTEGER_CLASS_YEAR, status().isOk(), studentsInValidIntegerClassYear);
  }

  @Test
  public void testDoubleClassYearWithinBounds() throws Exception {
    testGetStudents(VALID_DOUBLE_CLASS_YEAR, status().isOk(), studentsInValidDoubleClassYear);
  }

  @Test
  public void testInvalidDoubleClassYear() throws Exception {
    testGetStudents(INVALID_DOUBLE_CLASS_YEAR, status().isBadRequest(), "Year must be divisible by 0.5.");
  }

  @Test
  public void testNotFound() throws Exception {
    testGetStudents(OTHER_VALID_INTEGER_CLASS_YEAR, status().isOk(), Collections.emptySet());
  }
  
  @Test
  public void testClassYearOutOfBounds() throws Exception {
    testGetStudents(INVALID_INTEGER_CLASS_YEAR, status().isBadRequest(), "Year must be four digits.");
  }

  @Test
  public void testClassYearNull() throws Exception {
    testGetStudents(null, status().isBadRequest(), "Year cannot be null.");
  }

  @Test
  public void testClassYearNan() throws Exception {
    testGetStudents(NAN, status().isBadRequest(), "Year must be a number.");
  }
}

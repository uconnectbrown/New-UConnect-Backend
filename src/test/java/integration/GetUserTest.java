package integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.helper.UserTestUtil;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
            validUser = MockData.generateValidUser();

            init = false;
        }
    }

    @Test
    @Order(1)
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
    @Order(2)
    public void testUserNotFound() throws Exception {
        String invalidUsername = "no@exist.org";

        String token = UserTestUtil.getTokenForTraditionalUser(mockMvc, validUser, false, ddbAdapter, userTableName);
        UserTestUtil.getUser(mockMvc, validUser.getUsername(), invalidUsername, token)
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Requested user either does not exist or is not available at this time")));
    }
}

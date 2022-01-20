package integration;

import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DdbAdapterQueriesTest extends BaseIntTest {

    private boolean init = false;

    private final Class<User> clazz = User.class;

    private User user = MockData.generateValidUser();

    private User userWithId = MockData.generateValidUser();

    private List<User> userList = new ArrayList<>();

    @BeforeEach
    public void setup() {
        if (!init) {
            // Create user table
            ddbAdapter.createOnDemandTableIfNotExists(userTableName, clazz);

            // Add user data
            for (int i = 0; i < 10; i++) {
                userList.add(MockData.generateValidUser());
            }
            userWithId.setId("1234");

            init = false;
        }
    }

    @Test
    public void testCRUD() throws UserNotFoundException {
        for (User u : userList) {
            ddbAdapter.save(userTableName, u);
        }
        // Test that all users were added
        assertEquals(ddbAdapter.scan(userTableName, clazz).size(),
                userList.size());
        for (User u : userList) {
            assertEquals(ddbAdapter.findByUsername(u.getUsername()), u);
        }
        ddbAdapter.save(userTableName, user);

        for (User u : userList) {
            ddbAdapter.delete(userTableName, u);
        }
        // Check that all users in userList were deleted
        List<User> scanRes = ddbAdapter.scan(userTableName, clazz);
        assertEquals(scanRes.size(), 1);
        assertEquals(scanRes.get(0), user);
        ddbAdapter.delete(userTableName, user);
    }

    @Test
    public void testQuery() {
        ddbAdapter.save(userTableName, user);
        assertEquals(ddbAdapter.query(userTableName, user, clazz).get(0), user);
        ddbAdapter.delete(userTableName, user);
    }

    @Test
    public void testQueryGSI() {
        ddbAdapter.save(userTableName, user);
        User queryRes = ddbAdapter.queryGSI(userTableName,
                emailIndexName, user, clazz).get(0);
        assertEquals(queryRes, user);
        ddbAdapter.delete(userTableName, user);
    }

    @Test
    public void testFindByUsername() throws UserNotFoundException {
        assertThrows(UserNotFoundException.class,
                () -> ddbAdapter.findByUsername(user.getUsername()));
        ddbAdapter.save(userTableName, user);
        assertEquals(ddbAdapter.findByUsername(user.getUsername()), user);
        ddbAdapter.delete(userTableName, user);
    }

    @Test
    public void testFindAndExistsById() throws UserNotFoundException {
        assertFalse(ddbAdapter.existsById(userWithId.getId()));
        assertThrows(UserNotFoundException.class,
                () -> ddbAdapter.findById(userWithId.getId()));
        ddbAdapter.save(userTableName, userWithId);
        assertTrue(ddbAdapter.existsById(userWithId.getId()));
        assertEquals(ddbAdapter.findById(userWithId.getId()), userWithId);
        ddbAdapter.delete(userTableName, userWithId);
    }
}

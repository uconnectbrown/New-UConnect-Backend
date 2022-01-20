package integration;

import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DdbAdapterTableCreationTest extends BaseIntTest {

    private final Class<User> clazz = User.class;

    private final int rcu = 1, wcu = 1;

    @BeforeEach
    public void setup() {
        ddbAdapter.deleteTableIfExists(userTableName, clazz);
    }

    @Test
    public void testCreateTableIfNotExists() {
        // table does not exist
        assertTrue(ddbAdapter.createTableIfNotExists(userTableName, clazz, rcu, wcu));

        // create table when exists already
        assertFalse(ddbAdapter.createTableIfNotExists(userTableName, clazz, rcu, wcu));
    }

    @Test
    public void testCreateOnDemandTableIfNotExists() {
        assertTrue(ddbAdapter.createOnDemandTableIfNotExists(userTableName, clazz));
        assertFalse(ddbAdapter.createOnDemandTableIfNotExists(userTableName, clazz));
    }

    @Test
    public void testDeleteTableIfExists() {
        assertFalse(ddbAdapter.deleteTableIfExists(userTableName, clazz));
        ddbAdapter.createTableIfNotExists(userTableName, clazz, rcu, wcu);
        assertTrue(ddbAdapter.deleteTableIfExists(userTableName, clazz));
    }

}

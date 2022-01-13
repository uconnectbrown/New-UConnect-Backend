package integration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.uconnect.backend.UConnectBackendApplication;
import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.helper.BaseIntTest;
import com.uconnect.backend.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = UConnectBackendApplication.class)
public class DdbAdapterTableCreationTest extends BaseIntTest {

    @Autowired
    private String userTableName;

    @Autowired
    private DdbAdapter ddbAdapter;

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

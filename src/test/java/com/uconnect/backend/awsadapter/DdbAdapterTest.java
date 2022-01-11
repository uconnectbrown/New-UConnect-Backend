package com.uconnect.backend.awsadapter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class DdbAdapterTest extends BaseUnitTest {
    @Autowired
    private String userTableName;

    private final Class<User> clazz = User.class;

    private final int rcu = 1;

    private final int wcu = 1;

    private User user;

    private DdbAdapter ddbAdapter;

    @Mock
    private AmazonDynamoDB ddbClient;

    @BeforeEach
    public void setup() {
        ddbAdapter = new DdbAdapter(ddbClient);
        user = User.builder().id("1234").username("Testy").build();
    }

    @Test
    public void testCreateTableIfNotExists() {
        assertTrue(ddbAdapter.createTableIfNotExists(userTableName, clazz, rcu, wcu));
    }

    @Test
    public void testCreateOnDemandTableIfNotExists() {
        assertTrue(ddbAdapter.createOnDemandTableIfNotExists(userTableName, clazz));
    }

    @Test
    public void testDelete() {
        ddbAdapter.delete(userTableName, user);
    }
}

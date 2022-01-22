package com.uconnect.backend.user.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
public class TestUserDAO extends BaseUnitTest {
    @Autowired
    private String userTableName;

    @Autowired
    private String emailVerificationTableName;

    @Mock
    private DdbAdapter ddbAdapter;

    private UserDAO dao;

    private User user;

    private Set<String> pending;

    private Set<String> connections;

    private boolean init = false;

    @BeforeEach
    public void setup() {
        if (!init) {
            user = MockData.generateValidUser();
            pending = Collections.singleton(MockData.generateValidUser().getUsername());
            connections = Collections.singleton(MockData.generateValidUser().getUsername());
            user.setPending(pending);
            user.setConnections(connections);
            dao = new UserDAO(ddbAdapter, userTableName, emailVerificationTableName);
        }
    }

    @Test
    public void testGetUserByUsername() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        assertEquals(user, dao.getUserByUsername(user.getUsername()));
    }

    @Test
    public void testGetUserByUsernameNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> dao.getUserByUsername(user.getUsername()));
    }

    @Test
    public void testGetUserById() throws UserNotFoundException {
        when(ddbAdapter.findById(user.getId())).thenReturn(user);
        assertEquals(user, dao.getUserById(user.getId()));
    }

    @Test
    public void testGetUserByIdNotFound() throws UserNotFoundException {
        when(ddbAdapter.findById(user.getId())).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class,
            () -> dao.getUserById(user.getId()));
    }

    @Test
    public void testGetPasswordByUsername() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        assertEquals(user.getPassword(), dao.getPasswordByUsername(user.getUsername()));
    }

    @Test
    public void testDeleteUserSuccess() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        when(ddbAdapter.existsById(user.getId())).thenReturn(false);
        assertEquals(0, dao.deleteUser(user.getUsername()));
    }

    @Test
    public void testDeleteUserNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertEquals(-1, dao.deleteUser(user.getUsername()));
    }

    // deletion failure. user exists but ddbadapter/ddb failed to delete.
    @Test
    public void testDeleteFailure() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        when(ddbAdapter.existsById(user.getId())).thenReturn(true);
        assertEquals(-2, dao.deleteUser(user.getUsername()));
    }

    @Test
    public void testGetPending() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        assertEquals(pending, dao.getPending(user.getUsername()));
    }

    @Test
    public void testGetPendingNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertNull(dao.getPending(user.getUsername()));
    }

    @Test
    public void testGetConnections() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername())).thenReturn(user);
        assertEquals(connections, dao.getConnections(user.getUsername()));
        return;
    }

    @Test
    public void testGetConnectionsNotFound() throws UserNotFoundException {
        when(ddbAdapter.findByUsername(user.getUsername()))
            .thenThrow(UserNotFoundException.class);
        assertNull(dao.getConnections(user.getUsername()));
    }
}

package com.uconnect.backend.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.helper.MockData;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
public class TestUserService extends BaseUnitTest {

    @Mock
    private UserDAO dao;

    private UserService userService;

    private User user;

    private Set<String> pending;

    private Set<String> connections;

    private boolean init = false;

    @BeforeEach
    public void setup() {
        if (!init) {
            user = MockData.generateValidUser();
            userService = new UserService(dao, jwtUtility, null, null);
            pending = Collections.singleton(MockData.generateValidUser().getUsername());
            connections = Collections.singleton(MockData.generateValidUser().getUsername());
            user.setPending(pending);
            user.setConnections(connections);
            init = true;
        }
    }

    // loadUserByUsername()
    @Test
    public void testLoadUserByUsername() throws UserNotFoundException {
        when(dao.getUserByUsername(user.getUsername()))
            .thenReturn(user);
        assertEquals(user, userService.loadUserByUsername(user.getUsername()));
    }

    @Test
    public void testLoadUserByUsernameNotFound() throws UserNotFoundException {
        when(dao.getUserByUsername(user.getUsername()))
            .thenThrow(new UserNotFoundException(user.getUsername()));
        assertThrows(UsernameNotFoundException.class,
            () -> userService.loadUserByUsername(user.getUsername()));
    }

    // createNewUser()
    @Test
    public void testCreateNewUser() throws UserNotFoundException {
        when(dao.getUserByUsername(user.getUsername()))
            .thenThrow(new UserNotFoundException(user.getUsername()));
        assertEquals(0, userService.createNewUser(user));
    }

    @Test
    public void testCreateNewUserExistsAlready() throws UserNotFoundException {
        when(dao.getUserByUsername(user.getUsername()))
            .thenReturn(user);
        assertEquals(-1, userService.createNewUser(user));
    }

    @Test
    public void testCreateNewUserUnexpectedException() throws UserNotFoundException {
        when(dao.getUserByUsername(user.getUsername()))
            .thenThrow(new RuntimeException());
        assertEquals(-2, userService.createNewUser(user));
    }

    // deleteUser()
    @Test
    public void testDeleteUser() {
        // tests each exit code from dao
        final int SMALLEST_DAO_EXIT_CODE = -2;
        for (int exitCode = 0; exitCode >= SMALLEST_DAO_EXIT_CODE; exitCode--) {
            when(dao.deleteUser(user.getUsername())).thenReturn(exitCode);
            assertEquals(exitCode, userService.deleteUser(user.getUsername()));
        }
    }
    
    @Test
    public void testDeleteUserUnexpectedException() {
        when(dao.deleteUser(user.getUsername())).thenThrow(RuntimeException.class);
        assertEquals(-3, userService.deleteUser(user.getUsername()));
    }

    // getPending()
    @Test
    public void testGetPending() {
        when(dao.getPending(user.getUsername())).thenReturn(user.getPending());
        assertEquals(pending, userService.getPending(user.getUsername()));
    }

    // getConnections()
    @Test
    public void testGetConnections() {
        when(dao.getConnections(user.getUsername())).thenReturn(user.getConnections());
        assertEquals(connections, userService.getConnections(user.getUsername()));
    }
}

package com.uconnect.backend.user.service;

import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserDAO dao;

    @Autowired
    public UserService(UserDAO dao) {
        this.dao = dao;
    }

    @Override
    public User loadUserByUsername(String username) {
        return dao.getUserByUsername(username);
    }

    /**
     * Creates a new user.
     * <p>
     * Returns -2 in case of unexpected exception.
     *
     * @param username    The username of the user to create
     * @param rawPassword The raw password of the user
     * @param user        A user object to populate the remainder of the user's data
     * @return An exit code
     */
    public int createNewUser(String username, String rawPassword, User user) {
        try {
            return dao.createNewUser(username, rawPassword, user);
        } catch (Exception e) {
            log.error("Unexpected exception while creating new user " + username + ": {}", e);
            return -2;
        }
    }

    /**
     * Updates an existing user.
     * <p>
     * Returns -2 in case of unexpected exception.
     * 
     * @param username    The username of the user to update
     * @param rawPassword The raw password of the user to update
     * @param user        The user to update
     * @return An exit code
     */
    public int updateUser(String username, String rawPassword, User user) {
        try {
            return dao.updateUser(username, rawPassword, user);
        } catch (Exception e) {
            log.error("Unexpected exception while updating existing user " + username + ": {}", e);
            return -2;
        }
    }

    /**
     * Deletes a user.
     * <p>
     * Returns -3 in case of unexpected exception.
     *
     * @param username The username of the user to delete
     * @return An exit code
     */
    public int deleteUser(String username) {
        try {
            return dao.deleteUser(username);
        } catch (Exception e) {
            log.error("Unexpected exception while deleting user " + username + ": {}", e);
            return -3;
        }
    }

    public Set<String> getPending(String username) {
        return dao.getPending(username);
    }

    public Set<String> getConnections(String username) {
        return dao.getConnections(username);
    }
}

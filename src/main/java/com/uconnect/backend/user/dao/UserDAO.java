package com.uconnect.backend.user.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class UserDAO {
    private final DdbAdapter ddbAdapter;

    private final PasswordEncoder passwordEncoder;

    private final String userTableName;

    @Autowired
    public UserDAO(DdbAdapter ddbAdapter, PasswordEncoder passwordEncoder, String userTableName) {
        this.ddbAdapter = ddbAdapter;
        this.passwordEncoder = passwordEncoder;
        this.userTableName = userTableName;
    }

    public User getUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = ddbAdapter.findByUsername(username);
            return user;
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(String.format("%s is not a valid username", username));
        }
    }

    public User getUserById(String id) throws UserNotFoundException {
        return ddbAdapter.findById(id);
    }

    public String getPasswordByUsername(String username) throws UserNotFoundException {
        User user = ddbAdapter.findByUsername(username);

        return user.getPassword();
    }

    public int createNewUser(String username, String rawPassword, User user) {
        try {
            ddbAdapter.findByUsername(username);

            // user exists
            return -1;
        } catch (UserNotFoundException e) {
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));

            ddbAdapter.save(userTableName, user);

            // successfully created a new user
            return 0;
        }
    }

    public int deleteUser(String username) {
        try {
            User userToDelete = ddbAdapter.findByUsername(username);
            String id = userToDelete.getId();
            ddbAdapter.delete(userTableName, userToDelete);

            // successfully created a new user
            return ddbAdapter.existsById(id) ? -2 : 0;
        } catch (UserNotFoundException e) {
            // username does not exist
            return -1;
        }
    }

    /**
     * Gets the pending connections of the specified user.
     *
     * @param username The username of the user
     * @return If the user exists, return the set of pending connections; otherwise,
     *         return null
     */
    public Set<String> getPending(String username) {
        try {
            User user = ddbAdapter.findByUsername(username);
            return user.getPending();
        } catch (UserNotFoundException e) {
            // user does not exist
            return null;
        }
    }

    /**
     * Gets the connections of the specified user.
     *
     * @param username The username of the user
     * @return If the user exists, return the set of connections; otherwise, return
     *         null
     */
    public Set<String> getConnections(String username) {
        try {
            User user = ddbAdapter.findByUsername(username);
            return user.getConnections();
        } catch (UserNotFoundException e) {
            // user does not exist
            return null;
        }
    }
}

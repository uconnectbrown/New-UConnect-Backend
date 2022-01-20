package com.uconnect.backend.user.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.user.model.EmailVerification;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class UserDAO {
    private final DdbAdapter ddbAdapter;

    private final String userTableName;

    private final String emailVerificationTableName;

    @Autowired
    public UserDAO(DdbAdapter ddbAdapter, String userTableName, String emailVerificationTableName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
        this.emailVerificationTableName = emailVerificationTableName;

        if ("dev".equals(System.getenv("SPRING_PROFILES_ACTIVE")) &&
                "true".equals(System.getenv("IS_MANUAL_TESTING"))) {
            // mirror prod tables if booting up locally for manual testing
            ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class);
            ddbAdapter.createOnDemandTableIfNotExists(emailVerificationTableName, EmailVerification.class);
        }
    }

    public User getUserByUsername(String username) throws UserNotFoundException {
        return ddbAdapter.findByUsername(username);
    }

    public User getUserById(String id) throws UserNotFoundException {
        return ddbAdapter.findById(id);
    }

    public String getPasswordByUsername(String username) throws UserNotFoundException {
        User user = ddbAdapter.findByUsername(username);

        return user.getPassword();
    }

    public void saveUser(User user) {
        ddbAdapter.save(userTableName, user);
    }

    // TODO: Redesign updateUser API @Jake
    // /**
    //  * Update the details of an existing user.
    //  * 
    //  * @param username The username of the user to update
    //  * @param rawPassword The raw password of the user to update
    //  * @param user The user to update
    //  * @return Returns 0 if successful and -1 otherwise (user does not exist)
    //  * @throws UserNotFoundException If the user is not found
    //  */
    // public int updateUser(String username, String rawPassword, User user) throws UserNotFoundException {
    //     if (ddbAdapter.findByUsername(username) == null) {
    //         // user does not exist
    //         return -1;
    //     }

    //     user.setUsername(username);
    //     user.setPassword(passwordEncoder.encode(rawPassword));

    //     ddbAdapter.save(userTableName, user);

    //     return 0;
    // }

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
     * return null
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
     * null
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

    /**
     * Set the expected verification code for the given email address. Delete the entry if code is null.
     * @param emailAddress
     * @param code
     */
    public void setEmailVerificationCode(String emailAddress, String code) {
        EmailVerification emailVerification = EmailVerification.builder()
                .emailAddress(emailAddress)
                .verificationCode(code)
                .build();

        if (code == null) {
            ddbAdapter.delete(emailVerificationTableName, emailVerification);
            return;
        }

        ddbAdapter.save(emailVerificationTableName, emailVerification);
    }

    public String getEmailVerificationCode(String emailAddress) {
        EmailVerification emailVerification = EmailVerification.builder()
                .emailAddress(emailAddress)
                .build();
        List<EmailVerification> verifications =
                ddbAdapter.query(emailVerificationTableName, emailVerification, EmailVerification.class);

        if (verifications.isEmpty()) {
            return null;
        }

        return verifications.get(0).getVerificationCode();
    }
}

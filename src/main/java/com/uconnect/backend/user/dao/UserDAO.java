package com.uconnect.backend.user.dao;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
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
    private final String emailIndexName;

    @Autowired
    public UserDAO(DdbAdapter ddbAdapter, String userTableName, String emailVerificationTableName, String emailIndexName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
        this.emailVerificationTableName = emailVerificationTableName;
        this.emailIndexName = emailIndexName;

        if ("dev".equals(System.getenv("SPRING_PROFILES_ACTIVE")) &&
                "true".equals(System.getenv("IS_MANUAL_TESTING"))) {
            // mirror prod tables if booting up locally for manual testing
            ddbAdapter.createOnDemandTableIfNotExists(userTableName, User.class);
            ddbAdapter.createOnDemandTableIfNotExists(emailVerificationTableName, EmailVerification.class);
        }
    }

    public User getUserByUsername(String username) throws UserNotFoundException {
            User desiredUser = new User();
            desiredUser.setUsername(username);
            List<User> res = ddbAdapter.queryGSI(userTableName, emailIndexName, desiredUser, User.class);
            if (res.isEmpty()) {
                throw new UserNotFoundException("User not found with username " + username);
            }
            System.out.println(res.get(0));
            return res.get(0);
        }


    public User getUserById(String id) throws UserNotFoundException {
        User user = User.builder().id(id).build();
        List<User> userList = ddbAdapter.query(userTableName, user, User.class);
        if (userList.isEmpty()) {
            throw new UserNotFoundException("User not found with ID " + id);
        }

        return userList.get(0);
    }

    public String getPasswordByUsername(String username) throws UserNotFoundException {
        User user = getUserByUsername(username);

        return user.getPassword();
    }

    public void saveUser(User user) {
        ddbAdapter.save(userTableName, user);
    }

    /**
     * Deletes a user from the database.
     * <p>
     * Returns one of the following exit codes:
     * <ul>
     * <li> 0 indicates successful deletion </li>
     * <li> -1 indicates username does not exist </li>
     * <li> -2 indicates failure to delete </li>
     * </ul>
     *
     * @param username The username of the user to delete
     * @return An exit code
     */
    public int deleteUser(String username) {
        try {
            User userToDelete = getUserByUsername(username);
            String id = userToDelete.getId();
            ddbAdapter.delete(userTableName, userToDelete);

            // successfully created a new user
            return UserExistsById(id) ? -2 : 0;
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
            User user = getUserByUsername(username);
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
            User user = getUserByUsername(username);
            return user.getConnections();
        } catch (UserNotFoundException e) {
            // user does not exist
            return null;
        }
    }

    /**
     * Set the expected verification code for the given email address. Delete the entry if code is null.
     *
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
    //same as above
    public boolean UserExistsById(String id) {
        User user = User.builder().id(id).build();
        List<User> userList = ddbAdapter.query(userTableName, user, User.class );
        return !userList.isEmpty();
    }
}

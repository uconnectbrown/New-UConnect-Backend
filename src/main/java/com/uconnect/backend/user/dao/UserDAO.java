package com.uconnect.backend.user.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class UserDAO {

    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${amazon.dynamodb.tablename.users")
    private String tableName;

    public String getPasswordByUsername(String username) {
        User user = ddbAdapter.findByUsername(username);

        return user.getPassword();
    }

    public int createNewUser(String username, String rawPassword, String emailAddress, Set<String> nl, Set<String> ll) {
        if (ddbAdapter.findByUsername(username) != null) {
            // username exists
            return -1;
        }

        if (ddbAdapter.findByEmailAddress(emailAddress) != null) {
            // email address exists
            return -2;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmailAddress(emailAddress);
        user.setNativeLanguages(nl);
        user.setLearningLanguages(ll);

        ddbAdapter.save(tableName, user);

        // successfully created a new user
        return 0;
    }

    public int deleteUser(String username) {
        User userToDelete = ddbAdapter.findByUsername(username);

        if (userToDelete == null) {
            // username does not exist
            return -1;
        }

        String id = userToDelete.getId();
        ddbAdapter.delete(tableName, userToDelete);

        // successfully created a new user
        return ddbAdapter.existsById(id) ? -2 : 0;
    }
}


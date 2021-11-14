package com.uconnect.backend.user.dao;

import java.util.List;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class UserDAO {
    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private String userTableName;

    public User getUserByUsername(String username) {
        User user = ddbAdapter.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("%s is not a valid username", username));
        }

        return user;
    }

    public User getUserById(String id) {
        return ddbAdapter.findById(id);
    }

    public String getPasswordByUsername(String username) {
        User user = ddbAdapter.findByUsername(username);

        return user.getPassword();
    }

    public int createNewUser(String username, String rawPassword, String firstName, String lastName, String classYear,
            List<String> majors, String pronouns, List<String> location, List<String> interests) {
        if (ddbAdapter.findByUsername(username) != null) {
            // username exists
            return -1;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));

        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setClassYear(classYear);
        user.setMajors(majors);
        user.setPronouns(pronouns);
        user.setLocation(location);
        user.setInterests(interests);

        ddbAdapter.save(userTableName, user);

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
        ddbAdapter.delete(userTableName, userToDelete);

        // successfully created a new user
        return ddbAdapter.existsById(id) ? -2 : 0;
    }
}

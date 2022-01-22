package com.uconnect.backend.post.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.post.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class UserDAO {
    private final DdbAdapter ddbAdapter;


    private final String userTableName;

    @Autowired
    public UserDAO(DdbAdapter ddbAdapter, String userTableName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
    }

    public Post getUserByUsername(String username) throws UserNotFoundException {
        return ddbAdapter.findByUsername(username);
    }

    public Post getUserById(String id) throws UserNotFoundException {
        return ddbAdapter.findById(id);
    }

    public String getPasswordByUsername(String username) throws UserNotFoundException {
        Post post = ddbAdapter.findByUsername(username);

        return post.getPassword();
    }

    public void saveUser(Post post) {
        ddbAdapter.save(userTableName, post);
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
            Post postToDelete = ddbAdapter.findByUsername(username);
            String id = postToDelete.getId();
            ddbAdapter.delete(userTableName, postToDelete);

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
            Post post = ddbAdapter.findByUsername(username);
            return post.getPending();
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
            Post post = ddbAdapter.findByUsername(username);
            return post.getConnections();
        } catch (UserNotFoundException e) {
            // user does not exist
            return null;
        }
    }
}

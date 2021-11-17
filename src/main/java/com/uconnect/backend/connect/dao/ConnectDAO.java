package com.uconnect.backend.connect.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.user.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ConnectDAO {
    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private String userTableName;

    /**
     * Creates a request to connect from user1 to user2.
     * 
     * Exit codes:
     *   0 -- normal termination
     *   -1 -- user1 has already sent a request to user2
     *   -2 -- user2 has already received a request from user1
     *   -3 -- user1 does not have a sufficient number of requests
     * 
     * The operation is only successful on normal termination. All other cases will
     * result in no changes to user1 and user2 in the database.
     * 
     * @param username1 The username of user1
     * @param username2 The username of user2
     * @return An exit code
     */
    public int request(String username1, String username2) {
        User user1 = ddbAdapter.findByUsername(username1);
        User user2 = ddbAdapter.findByUsername(username2);

        // Add user2 to user1's sent list
        if (user1.getSent().contains(username2)) {
            return -1; // user1 has already sent a request to user2
        } else {
            user1.getSent().add(username2);
        }

        // Add user1 to user2's pending list
        if (user2.getPending().contains(username1)) {
            return -2; // should not get here in theory
        } else {
            user2.getPending().add(username1);
        }

        // Decrement user1's requests
        int requests = user1.getRequests();
        if (requests < 1) {
            return -3; // user1 does not have a sufficient number of requests
        } else {
            user1.setRequests(--requests);
        }

        // Save
        ddbAdapter.save(userTableName, user1);
        ddbAdapter.save(userTableName, user2);

        return 0;
    }

    /**
     * Undoes a request to connect from user1 to user2.
     * 
     * Exit codes:
     *   0 -- normal termination
     *   -1 -- user1 has not sent a request to user2
     *   -2 -- user2 has not received a request from user1
     *   -3 -- user1 has too many requests
     * 
     * The operation is only successful on normal termination. All other cases will
     * result in no changes to user1 and user2 in the database.
     * 
     * @param username1 The username of user1
     * @param username2 The username of user2
     * @return An exit code
     */
    public int undoRequest(String username1, String username2) {
        User user1 = ddbAdapter.findByUsername(username1);
        User user2 = ddbAdapter.findByUsername(username2);

        // Remove user2 from user1's sent list
        if (!(user1.getSent().contains(username2))) {
            return -1; // user1 has already sent a request to user2
        } else {
            user1.getSent().remove(username2);
        }

        // Remove user1 from user2's pending list
        if (!(user2.getPending().contains(username1))) {
            return -2; // should not get here in theory
        } else {
            user2.getPending().remove(username1);
        }

        // Increment user1's requests
        int requests = user1.getRequests();
        if (requests > 9) {
            return -3; // user1 has too many requests (should not happen)
        } else {
            user1.setRequests(++requests);
        }

        // Save
        ddbAdapter.save(userTableName, user1);
        ddbAdapter.save(userTableName, user2);

        return 0;
    }
}

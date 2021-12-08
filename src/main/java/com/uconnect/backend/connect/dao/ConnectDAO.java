package com.uconnect.backend.connect.dao;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.user.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class ConnectDAO {
    private final DdbAdapter ddbAdapter;

    private final String userTableName;

    @Autowired
    public ConnectDAO(DdbAdapter ddbAdapter, String userTableName) {
        this.ddbAdapter = ddbAdapter;
        this.userTableName = userTableName;
    }

    /**
     * Creates a request to connect from sender to receiver.
     * <p>
     * Exit codes:
     * 0 -- normal termination
     * -1 -- sender has already sent a request to receiver
     * -2 -- receiver has already received a request from sender
     * -3 -- sender does not have a sufficient number of requests
     * <p>
     * The operation is only successful on normal termination. All other cases will
     * result in no changes to sender and receiver in the database.
     *
     * @param senderUsername   The username of sender
     * @param receiverUsername The username of receiver
     * @return An exit code
     */
    public int request(String senderUsername, String receiverUsername) throws UserNotFoundException {
        User sender = ddbAdapter.findByUsername(senderUsername);
        User receiver = ddbAdapter.findByUsername(receiverUsername);

        // Add receiver to sender's sent list
        if (sender.getSent().contains(receiverUsername)) {
            return -1; // sender has already sent a request to receiver
        } else {
            sender.getSent().add(receiverUsername);
        }

        // Add sender to receiver's pending list
        if (receiver.getPending().contains(senderUsername)) {
            return -2; // should not get here in theory
        } else {
            receiver.getPending().add(senderUsername);
        }

        // Decrement sender's requests
        int requests = sender.getRequests();
        if (requests < 1) {
            log.info("User " + senderUsername + " did not have enough requests to send a request to "
                    + receiverUsername);
            return -3; // sender does not have a sufficient number of requests
        } else {
            sender.setRequests(--requests);
        }

        // Save
        ddbAdapter.save(userTableName, sender);
        ddbAdapter.save(userTableName, receiver);

        return 0;
    }

    /**
     * Undoes a request to connect from sender to receiver.
     * <p>
     * Exit codes:
     * 0 -- normal termination
     * -1 -- sender has not sent a request to receiver
     * -2 -- receiver has not received a request from sender
     * -3 -- sender has too many requests
     * <p>
     * The operation is only successful on normal termination. All other cases will
     * result in no changes to sender and receiver in the database.
     *
     * @param senderUsername   The username of sender
     * @param receiverUsername The username of receiver
     * @return An exit code
     */
    public int undoRequest(String senderUsername, String receiverUsername) throws UserNotFoundException {
        User sender = ddbAdapter.findByUsername(senderUsername);
        User receiver = ddbAdapter.findByUsername(receiverUsername);

        // Remove receiver from sender's sent list
        if (!(sender.getSent().contains(receiverUsername))) {
            return -1; // sender has already sent a request to receiver
        } else {
            sender.getSent().remove(receiverUsername);
        }

        // Remove sender from receiver's pending list
        if (!(receiver.getPending().contains(senderUsername))) {
            return -2; // should not get here in theory
        } else {
            receiver.getPending().remove(senderUsername);
        }

        // Increment sender's requests
        int requests = sender.getRequests();
        if (requests > 9) {
            log.info("User " + senderUsername + " has " + requests + " requests, which is more than the maximum");
            return -3; // sender has too many requests (should not happen)
        } else {
            sender.setRequests(++requests);
        }

        // Save
        ddbAdapter.save(userTableName, sender);
        ddbAdapter.save(userTableName, receiver);

        return 0;
    }

    /**
     * Accept the request from sender to receiver.
     * <p>
     * Exit codes:
     * 0 -- normal termination
     * -1 -- receiver has no request from sender
     * -2 -- sender has not sent a request to receiver
     * -3 -- receiver already connected with sender
     * -4 -- sender already connected with receiver
     * -5 -- sender has too many requests
     * <p>
     * The operation is only successful on normal termination. All other cases will
     * result in no changes to sender and receiver in the database.
     * 
     * @param senderUsername   The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int accept(String senderUsername, String receiverUsername) {
        // Get sender and receiver
        User sender = ddbAdapter.findByUsername(senderUsername);
        User receiver = ddbAdapter.findByUsername(receiverUsername);

        // Remove sender from receiver's pending set
        if (!receiver.getPending().remove(senderUsername)) {
            return -1; // receiver never got a request from sender
        } else {
        }

        // Remove receiver from sender's sent set
        if (!sender.getSent().remove(receiverUsername)) {
            return -2; // sender never sent request to receiver
        }

        // Add sender to receiver's connection set
        if (!receiver.getConnections().add(senderUsername)) {
            return -3; // receiver already connected with sender
        }

        // Add receiver to sender's connection set
        if (!sender.getConnections().add(receiverUsername)) {
            return -4; // sender already connected with receiver
        }

        // Increment sender's requests
        int requests = sender.getRequests();
        if (requests > 9) {
            log.info("User " + senderUsername + " has " + requests + " requests, which is more than the maximum");
            return -3; // sender has too many requests (should not happen)
        } else {
            sender.setRequests(++requests);
        }

        // Save
        ddbAdapter.save(userTableName, sender);
        ddbAdapter.save(userTableName, receiver);

        return 0;
    }
}

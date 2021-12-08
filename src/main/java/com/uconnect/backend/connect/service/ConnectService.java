package com.uconnect.backend.connect.service;

import com.uconnect.backend.connect.dao.ConnectDAO;
import com.uconnect.backend.exception.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConnectService {
    private final ConnectDAO dao;

    @Autowired
    public ConnectService(ConnectDAO dao) {
        this.dao = dao;
    }

    /**
     * Calls request() from ConnectDAO.
     * <p>
     * Returns -5 if an unexpected exception occurs, and returns -4 if user cannot
     * be found. Otherwise, returns the exit code of ConnectDAO.request().
     *
     * @param senderUsername   The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int request(String senderUsername, String receiverUsername) {
        try {
            return dao.request(senderUsername, receiverUsername);
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e);
            return -4;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return -5;
        }
    }

    /**
     * Calls undoRequest() from ConnectDAO.
     * <p>
     * Returns -5 if an unexpected exception occurs, and returns -4 if user cannot
     * be found. Otherwise, returns the exit code of ConnectDAO.undoRequest().
     *
     * @param senderUsername   The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int undoRequest(String senderUsername, String receiverUsername) {
        try {
            return dao.undoRequest(senderUsername, receiverUsername);
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", e);
            return -4;
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return -5;
        }
    }

    /**
     * Calls accept() from ConnectDAO.
     * <p>
     * Returns -6 if an unexpected exception occurs. Otherwise, returns the
     * exit code of ConnectDAO.accept().
     * 
     * @param senderUsername   The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int accept(String senderUsername, String receiverUsername) {
        try {
            return dao.accept(senderUsername, receiverUsername);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return -6;
        }
    }
}

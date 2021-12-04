package com.uconnect.backend.connect.service;

import com.uconnect.backend.connect.dao.ConnectDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ConnectService {
    @Autowired
    ConnectDAO dao;

    /**
     * Calls request() from ConnectDAO.
     * 
     * Returns -4 if an unexpected exception occurs. Otherwise, returns the
     * exit code of ConnectDAO.request().
     * 
     * @param senderUsername The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int request(String senderUsername, String receiverUsername) {
        try {
            return dao.request(senderUsername, receiverUsername);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return -4;
        }
    }

    /**
     * Calls undoRequest() from ConnectDAO.
     * 
     * Returns -4 if an unexpected exception occurs. Otherwise, returns the
     * exit code of ConnectDAO.undoRequest().
     * 
     * @param senderUsername The username of the sender
     * @param receiverUsername The username of the receiver
     * @return An exit code
     */
    public int undoRequest(String senderUsername, String receiverUsername) {
        try {
            return dao.undoRequest(senderUsername, receiverUsername);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e);
            return -4;
        }
    }
}

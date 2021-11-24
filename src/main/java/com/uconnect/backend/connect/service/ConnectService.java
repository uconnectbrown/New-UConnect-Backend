package com.uconnect.backend.connect.service;

import com.uconnect.backend.connect.dao.ConnectDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectService {
    @Autowired
    ConnectDAO dao;

    public int request(String senderUsername, String receiverUsername) {
        return dao.request(senderUsername, receiverUsername);
    }

    public int undoRequest(String senderUsername, String receiverUsername) {
        return dao.undoRequest(senderUsername, receiverUsername);
    }
}

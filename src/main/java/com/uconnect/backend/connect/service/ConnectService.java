package com.uconnect.backend.connect.service;

import com.uconnect.backend.connect.dao.ConnectDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConnectService {
    @Autowired
    ConnectDAO dao;

    public int request(String username1, String username2) {
        return dao.request(username1, username2);
    }

    public int undoRequest(String username1, String username2) {
        return dao.undoRequest(username1, username2);
    }
}

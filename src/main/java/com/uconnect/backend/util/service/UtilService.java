package com.uconnect.backend.util.service;

import java.util.List;

import com.uconnect.backend.util.dao.UtilDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UtilService {
    
    @Autowired
    UtilDAO dao;

    public List<String> getAllEmails() {
        return dao.getAllEmails();
    }
    
}

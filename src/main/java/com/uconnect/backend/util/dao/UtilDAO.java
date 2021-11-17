package com.uconnect.backend.util.dao;

import java.util.ArrayList;
import java.util.List;

import com.uconnect.backend.awsadapter.DdbAdapter;
import com.uconnect.backend.user.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UtilDAO {
    @Autowired
    private DdbAdapter ddbAdapter;

    @Autowired
    private String userTableName;
    
    /**
     * Get all of the emails currently in the database.
     * 
     * Calls to this function should be avoided, as it makes use of the DDB scan operation.
     * 
     * @return A list of all emails in the database
     */
    public List<String> getAllEmails() {
        List<String> emails = new ArrayList<>();
        List<User> users = ddbAdapter.scan(userTableName, User.class);
        for (User u : users) {
            emails.add(u.getUsername());
        }

        return emails;
    }

    /**
     * Get all emails that currently have a pending connection request.
     * 
     * Calls to this function should be avoided, as it makes use of the DDB scan operation.
     * 
     * @return A list of emails that have at least one pending connection request
     */
    public List<String> getAllPending() {
        List<String> emails = new ArrayList<>();
        List<User> users = ddbAdapter.scan(userTableName, User.class);
        for (User u : users) {
            if (!(u.getPending().isEmpty())) {
                emails.add(u.getUsername());
            }
        }

        return emails;
    } 
}

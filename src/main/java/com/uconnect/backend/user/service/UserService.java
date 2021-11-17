package com.uconnect.backend.user.service;

import java.util.List;

import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserDAO dao;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return dao.getUserByUsername(username);
    }

    public int createNewUser(String username, String rawPassword, User user) {
        return dao.createNewUser(username, rawPassword, user);
    }

    public int deleteUser(String username) {
        return dao.deleteUser(username);
    }

    public List<String> getPending(String username) {
        return dao.getPending(username);
    }

    public List<String> getConnections(String username) {
        return dao.getConnections(username);
    }
}


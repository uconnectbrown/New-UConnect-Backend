package com.uconnect.backend.user.service;

import com.uconnect.backend.user.dao.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserDAO dao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new User(username, dao.getPasswordByUsername(username), new ArrayList<>());
    }

    public int createNewUser(String username, String rawPassword, String emailAddress, Set<String> nl, Set<String> ll) {
        return dao.createNewUser(username, rawPassword, emailAddress, nl, ll);
    }

    public int deleteUser(String username) {
        return dao.deleteUser(username);
    }
}


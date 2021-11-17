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

    public int createNewUser(String username, String rawPassword, String firstName, String lastName, String classYear,
            List<String> majors, String pronouns, List<String> location, List<String> interests) {
        return dao.createNewUser(username, rawPassword, firstName, lastName, classYear, majors, pronouns, location, interests);
    }

    public int deleteUser(String username) {
        return dao.deleteUser(username);
    }
}


package com.uconnect.backend.user.controller;

import com.uconnect.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/signup/createNewUser")
    public ResponseEntity<String> createNewUser(@RequestBody Map<String, String> req) {
        Set<String> nlSet = new HashSet<String>(Arrays.asList(req.get("nl").split("-")));

        Set<String> llSet = new HashSet<String>(Arrays.asList(req.get("ll").split("-")));

        int result = userService.createNewUser((String) req.get("username"), (String) req.get("rawPassword"), (String) req.get("emailAddress"), nlSet, llSet);

        switch (result) {
            case 0:
                return new ResponseEntity<>("Successfully created a new account for " + (String) req.get("username"), HttpStatus.ACCEPTED);
            case -1:
                return new ResponseEntity<>("Failed to create a new account for " + (String) req.get("username") + ", USERNAME already exists", HttpStatus.BAD_REQUEST);
            case -2:
                return new ResponseEntity<>("Failed to create a new account for " + (String) req.get("username") + ", EMAIL ADDRESS already exists", HttpStatus.BAD_REQUEST);
            default:
                return new ResponseEntity<>("should not see this response, call your mother for me if you do", HttpStatus.I_AM_A_TEAPOT);
        }
    }

    @PostMapping("/api/userControl/deleteUser")
    public ResponseEntity<String> createNewUser(@RequestHeader(name = "Username") String username) {
        int result = userService.deleteUser(username);

        switch (result) {
            case 0:
                return new ResponseEntity<>("Successfully deleted account for " + username, HttpStatus.OK);
            case -1:
                return new ResponseEntity<>("Failed to delete account for " + username + ", USERNAME does not exist", HttpStatus.BAD_REQUEST);
            case -2:
                return new ResponseEntity<>("Something went wrong with our database, failed to delete account for " + username, HttpStatus.INTERNAL_SERVER_ERROR);
            default:
                return new ResponseEntity<>("should not see this response, call your mother for me if you do", HttpStatus.I_AM_A_TEAPOT);
        }
    }
}


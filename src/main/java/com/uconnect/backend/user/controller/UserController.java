package com.uconnect.backend.user.controller;

import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/api/signup/createNewUser")
    @SuppressWarnings("unchecked") // For casting request fields
    public ResponseEntity<String> createNewUser(@RequestBody Map<String, Object> req) {
        String username = (String) req.get("username");
        String rawPassword = (String) req.get("rawPassword");
        User user = User.builder()
            .firstName((String) req.get("firstName"))
            .lastName((String) req.get("lastName"))
            .classYear((String) req.get("classYear"))
            .majors((List<String>) req.get("majors"))
            .pronouns((String) req.get("pronouns"))
            .location((List<String>) req.get("location"))
            .interests((List<String>) req.get("interests"))
            .build();
        int result = userService.createNewUser(username, rawPassword, user);

        // TODO: Give user a default profile picture

        switch (result) {
            case 0:
                return new ResponseEntity<>("Successfully created a new account for " + req.get("username"), HttpStatus.ACCEPTED);
            case -1:
                return new ResponseEntity<>("Failed to create a new account for " + req.get("username") + ", USERNAME/EMAIL already exists", HttpStatus.BAD_REQUEST);
            default:
                return new ResponseEntity<>("should not see this response, call your mother for me if you do", HttpStatus.I_AM_A_TEAPOT);
        }
    }

    @PostMapping("/api/userControl/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestHeader(name = "Username") String username) {
        int result = userService.deleteUser(username);

        switch (result) {
            case 0:
                return new ResponseEntity<>("Successfully deleted account for " + username, HttpStatus.OK);
            case -1:
                return new ResponseEntity<>("Failed to delete account for " + username + ", USERNAME/EMAIL does not exist", HttpStatus.BAD_REQUEST);
            case -2:
                return new ResponseEntity<>("Something went wrong with our database, failed to delete account for " + username, HttpStatus.INTERNAL_SERVER_ERROR);
            default:
                return new ResponseEntity<>("should not see this response, call your mother for me if you do", HttpStatus.I_AM_A_TEAPOT);
        }
    }
}


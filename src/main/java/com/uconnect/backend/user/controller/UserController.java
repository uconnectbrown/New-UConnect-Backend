package com.uconnect.backend.user.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    private ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);

    /**
     * Endpoint for creating a new user. 
     * 
     * Takes in a request with the following fields:
     *   - username -- String
     *   - rawPassword -- String
     *   - firstName -- String
     *   - lastName -- String
     *   - classYear -- String
     *   - majors -- List<String>
     *   - pronouns -- String
     *   - location -- Location {String country, String state, String city}
     *   - interests -- List<String>
     *   - sent -- List<String>
     *   - pending -- List<String>
     *   - connections -- List<String>
     *   - requests -- int
     * 
     * @param req The request to the endpoint
     * @return An HTTP response
     */
    @PostMapping("/api/signup/createNewUser")
    public ResponseEntity<String> createNewUser(@RequestBody Map<String, Object> req) {
        String username = (String) req.get("username");
        String rawPassword = (String) req.get("rawPassword");

        User user = mapper.convertValue(req, User.class);

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

    /**
     * Gets the pending connections for the specified user.
     * 
     * Responds with NOT_FOUND if the specified user does not exist.
     * 
     * @param username The username of the user
     * @return A list of pending connections for the user
     */
    @GetMapping("/v1/user/getPending")
    public ResponseEntity<List<String>> getPending(@RequestHeader(name = "Username") String username) {
        List<String> pending = userService.getPending(username);

        return new ResponseEntity<>(pending, (pending == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }

    /**
     * Gets the connections for the specified user.
     * 
     * Responds with NOT_FOUND if the specified user does not exist.
     * 
     * @param username The username of the user
     * @return A list of connections for the user
     */
    @GetMapping("/v1/user/getConnections")
    public ResponseEntity<List<String>> getConnections(@RequestHeader(name = "Username") String username) {
        List<String> connections = userService.getConnections(username);

        return new ResponseEntity<>(connections, (connections == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
    }
}

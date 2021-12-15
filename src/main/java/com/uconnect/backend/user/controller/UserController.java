package com.uconnect.backend.user.controller;

import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import com.uconnect.backend.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtility jwtUtility;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtUtility jwtUtility) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtility = jwtUtility;
    }

    /**
     * Endpoint for creating a new user.
     * <p>
     * Takes in a request with the following fields:
     * - username -- String
     * - rawPassword -- String
     * - firstName -- String
     * - lastName -- String
     * - classYear -- String
     * - majors -- List<String>
     * - pronouns -- String
     * - location -- Location {String country, String state, String city}
     * - interests -- List<String>
     * - sent -- List<String>
     * - pending -- List<String>
     * - connections -- List<String>
     * - requests -- int
     *
     * @param user The request deserialized into a User object after validation
     * @return An HTTP response
     */
    @PostMapping(value = "/v1/user/signup/createNewUserTraditional")
    public ResponseEntity<String> createNewUser(@Valid @RequestBody User user) {
        try {
            String username = user.getUsername();
            String rawPassword = user.getPassword();

            user.setCreationType(UserCreationType.Traditional);

            int result = userService.createNewUser(username, rawPassword, user);

            // TODO: Give user a default profile picture

            switch (result) {
                case 0:
                    return new ResponseEntity<>("Successfully created a new account for " + username,
                            HttpStatus.ACCEPTED);
                case -1:
                    return new ResponseEntity<>(
                            "Failed to create a new account for " + username + ", USERNAME/EMAIL already exists",
                            HttpStatus.BAD_REQUEST);
                case -2:
                    return new ResponseEntity<>("Unexpected exception occurred when creating account for " + username,
                            HttpStatus.INTERNAL_SERVER_ERROR);
                default:
                    return new ResponseEntity<>("should not see this response, call your mother for me if you do",
                            HttpStatus.I_AM_A_TEAPOT);
            }
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            return new ResponseEntity<>("Unexpected exception occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/v1/user/authenticate/authenticateTraditional")
    public JwtResponse authenticate(@Valid @RequestBody JwtRequest jwtRequest) {
        // failed authentication exceptions handled by ExceptionHandlers
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                jwtRequest.getUsername(),
                jwtRequest.getPassword()
        ));

        final User user = userService.loadUserByUsername(jwtRequest.getUsername());

        final String token = jwtUtility.generateToken(user);

        return new JwtResponse(token);
    }

    @PostMapping("/api/userControl/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestHeader(name = "Username") String username) {
        int result = userService.deleteUser(username);

        switch (result) {
            case 0:
                return new ResponseEntity<>("Successfully deleted account for " + username, HttpStatus.OK);
            case -1:
                return new ResponseEntity<>(
                        "Failed to delete account for " + username + ", USERNAME/EMAIL does not exist",
                        HttpStatus.BAD_REQUEST);
            case -2:
                return new ResponseEntity<>(
                        "Something went wrong with our database, failed to delete account for " + username,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            case -3:
                return new ResponseEntity<>("Unexpected exception occurred when deleting account for " + username,
                        HttpStatus.INTERNAL_SERVER_ERROR);
            default:
                return new ResponseEntity<>("should not see this response, call your mother for me if you do",
                        HttpStatus.I_AM_A_TEAPOT);
        }
    }

    /**
     * Gets the pending connections for the specified user.
     * <p>
     * Responds with NOT_FOUND if the specified user does not exist.
     * Responds with INTERNAL_SERVER_ERROR if an unexpected exception is
     * encountered.
     *
     * @param username The username of the user
     * @return A list of pending connections for the user
     */
    @GetMapping("/v1/user/getPending")
    public ResponseEntity<Set<String>> getPending(@RequestHeader(name = "Username") String username) {
        try {
            Set<String> pending = userService.getPending(username);
            return new ResponseEntity<>(pending, (pending == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when getting pending connections for user " + username, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Gets the connections for the specified user.
     * <p>
     * Responds with NOT_FOUND if the specified user does not exist.
     * Responds with INTERNAL_SERVER_ERROR if an unexpected exception is
     * encountered.
     *
     * @param username The username of the user
     * @return A list of connections for the user
     */
    @GetMapping("/v1/user/getConnections")
    public ResponseEntity<Set<String>> getConnections(@RequestHeader(name = "Username") String username) {
        try {
            Set<String> connections = userService.getConnections(username);
            return new ResponseEntity<>(connections, (connections == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when getting connections for user " + username, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

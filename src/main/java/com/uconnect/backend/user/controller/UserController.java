package com.uconnect.backend.user.controller;

import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.security.RequestPermissionUtility;
import com.uconnect.backend.security.jwt.model.JwtRequest;
import com.uconnect.backend.security.jwt.model.JwtResponse;
import com.uconnect.backend.security.jwt.model.OAuthJwtResponse;
import com.uconnect.backend.security.oauth.OAuthRequest;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import com.uconnect.backend.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
import java.util.Set;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final RequestPermissionUtility requestPermissionUtility;

    @Autowired
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          RequestPermissionUtility requestPermissionUtility,
                          PasswordEncoder passwordEncoder,
                          ClientRegistrationRepository clientRegistrationRepository) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.requestPermissionUtility = requestPermissionUtility;
        this.passwordEncoder = passwordEncoder;
        this.clientRegistrationRepository = clientRegistrationRepository;
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
        String username = user.getUsername();
        String rawPassword = user.getPassword();

        userService.authorizeEmailDomain(username);

        if (rawPassword == null) {
            return new ResponseEntity<>(
                    "Failed to create a new account for " + username + ", password cannot be null",
                    HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setCreationType(UserCreationType.TRADITIONAL);
        user.setVerified(userService.checkProfileComplete(user));
        user.setCreatedAt(new Date());

        int result = userService.createNewUser(user);

        // TODO: Give user a default profile picture

        switch (result) {
            case 0:
                userService.startEmailVerification(username);

                return new ResponseEntity<>("Successfully created a new account for " + username,
                        HttpStatus.ACCEPTED);
            case 1:
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
    }

    @PostMapping("/v1/user/authenticate/traditional")
    public JwtResponse authenticateTraditional(@Valid @RequestBody JwtRequest jwtRequest) {
        String username = jwtRequest.getUsername();
        String password = jwtRequest.getPassword();

        log.info("User {} attempted to authenticate", username);

        // failed authentication exceptions handled by ExceptionHandlers
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                username,
                password
        ));

        String token = userService.generateTraditionalJWT(username);
        return new JwtResponse(token);
    }

    @PostMapping("/v1/user/authenticate/oauth/{registrationId}")
    public OAuthJwtResponse authenticateOAuth(HttpServletRequest request, @Valid @RequestBody OAuthRequest oAuthRequest,
                                              @PathVariable String registrationId) {
        String authCode = oAuthRequest.getAuthCode();
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(registrationId);

        OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken
                = userService.getOAuth2LoginAuthenticationToken(request, authCode, registrationId, registration);

        Authentication authResult = authenticationManager.authenticate(oAuth2LoginAuthenticationToken);

        String authenticatedUsername = ((DefaultOidcUser) authResult.getPrincipal()).getEmail();

        userService.authorizeEmailDomain(authenticatedUsername);

        String token = userService.generateOAuthJWT(authenticatedUsername);
        return new OAuthJwtResponse(token, authenticatedUsername);
    }

    @PostMapping("/v1/user/updateUser")
    public ResponseEntity<String> updateUser(@Valid @RequestBody User user) throws UserNotFoundException {
        String username = user.getUsername();
        requestPermissionUtility.authorizeUser(username);

        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userService.updateUser(user);

        return ResponseEntity.ok(String.format("Successfully updated user %s", username));
    }

    @PostMapping("/api/userControl/deleteUser")
    public ResponseEntity<String> deleteUser(@RequestHeader(name = "Username") String username) {
        requestPermissionUtility.authorizeUser(username);

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
            requestPermissionUtility.authorizeUser(username);
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
            requestPermissionUtility.authorizeUser(username);
            Set<String> connections = userService.getConnections(username);
            return new ResponseEntity<>(connections, (connections == null) ? HttpStatus.NOT_FOUND : HttpStatus.OK);
        } catch (Exception e) {
            log.error("Unexpected exception encountered when getting connections for user " + username, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/v1/user/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        User foundUser = userService.loadUserByUsername(username);
        foundUser.setPassword("");

        return ResponseEntity.status(HttpStatus.OK).body(foundUser);
    }

    @GetMapping("/v1/user/authenticate/emailVerification/{email}")
    public ResponseEntity<String> verifyEmail(@PathVariable String email, @RequestParam String code)
            throws UserNotFoundException {
        if (userService.validateEmailVerificationCode(email, code)) {
            userService.verifyUser(email);

            log.info("User {} has been verified", email);
            return ResponseEntity.accepted().build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification failed. Please contact us if this " +
                "happens repeatedly");
    }
}

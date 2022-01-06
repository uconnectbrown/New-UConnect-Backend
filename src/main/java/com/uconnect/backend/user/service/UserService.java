package com.uconnect.backend.user.service;

import com.uconnect.backend.exception.UnknownOAuthRegistrationException;
import com.uconnect.backend.exception.UnmatchedUserCreationTypeException;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserDAO dao;

    private final JwtUtility jwtUtility;

    private final OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    @Autowired
    public UserService(UserDAO dao,
                       JwtUtility jwtUtility,
                       OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver) {
        this.dao = dao;
        this.jwtUtility = jwtUtility;
        this.oAuth2AuthorizationRequestResolver = oAuth2AuthorizationRequestResolver;
    }

    @Override
    public User loadUserByUsername(String username) {
        try {
            return dao.getUserByUsername(username);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(String.format("%s is not a valid username", username));
        }
    }

    /**
     * Creates a new user.
     * <p>
     * Returns -2 in case of unexpected exception.
     *
     * @param user A user object to populate the remainder of the user's data
     * @return An exit code
     */
    public int createNewUser(User user) {
        try {
            String username = user.getUsername();
            User foundUser = dao.getUserByUsername(username);
            if (!foundUser.getCreationType().equals(UserCreationType.OAuth)) {
                return 1;
            }

            // user exists
            return -1;
        } catch (UserNotFoundException e) {
            dao.saveUser(user);

            // successfully created a new user
            return 0;
        } catch (Exception e) {
            log.error("Unexpected exception while creating new user " + user.getUsername() + ": {}", e);
            return -2;
        }
    }

    // TODO: Redesign updateUser API @Jake
    // /**
    //  * Updates an existing user.
    //  * <p>
    //  * Returns -2 in case of unexpected exception.
    //  * 
    //  * @param username    The username of the user to update
    //  * @param rawPassword The raw password of the user to update
    //  * @param user        The user to update
    //  * @return An exit code
    //  */
    // public int updateUser(String username, String rawPassword, User user) {
    //     try {
    //         return dao.updateUser(username, rawPassword, user);
    //     } catch (Exception e) {
    //         log.error("Unexpected exception while updating existing user " + username + ": {}", e);
    //         return -2;
    //     }
    // }

    /**
     * Deletes a user.
     * <p>
     * Returns -3 in case of unexpected exception.
     *
     * @param username The username of the user to delete
     * @return An exit code
     */
    public int deleteUser(String username) {
        try {
            return dao.deleteUser(username);
        } catch (Exception e) {
            log.error("Unexpected exception while deleting user " + username + ": {}", e);
            return -3;
        }
    }

    public Set<String> getPending(String username) {
        return dao.getPending(username);
    }

    public Set<String> getConnections(String username) {
        return dao.getConnections(username);
    }

    public String generateTraditionalJWT(String username) {

        final User user = loadUserByUsername(username);

        return jwtUtility.generateToken(user);
    }

    public String generateOAuthJWT(String username) {
        User user = loadUserByUsername(username);

        if (user == null) {
            // new user, create new record;
            user = User.builder()
                    .username(username)
                    .creationType(UserCreationType.OAuth)
                    .build();

            int result = createNewUser(user);
            if (result == 1) {
                // user was NOT created through OAuth
                throw new UnmatchedUserCreationTypeException();
            }
        }

        return jwtUtility.generateToken(user);
    }

    public OAuth2LoginAuthenticationToken getOAuth2LoginAuthenticationToken(HttpServletRequest request, String authCode,
                                                                            String registrationId, ClientRegistration registration) {
        if (registration == null) {
            throw new UnknownOAuthRegistrationException();
        }

        OAuth2AuthorizationRequest authorizationRequest = oAuth2AuthorizationRequestResolver.resolve(request, registrationId);
        OAuth2AuthorizationResponse authorizationResponse = OAuth2AuthorizationResponse
                .success(authCode)
                .redirectUri("/")
                .state(authorizationRequest.getState())
                .build();

        return new OAuth2LoginAuthenticationToken(registration,
                new OAuth2AuthorizationExchange(authorizationRequest, authorizationResponse));
    }
}

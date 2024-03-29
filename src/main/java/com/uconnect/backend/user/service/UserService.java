package com.uconnect.backend.user.service;

import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.awsadapter.SesAdapter;
import com.uconnect.backend.exception.DisallowedEmailDomainException;
import com.uconnect.backend.exception.UnknownOAuthRegistrationException;
import com.uconnect.backend.exception.UnmatchedUserCreationTypeException;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.model.UserCreationType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@NoArgsConstructor
public class UserService implements UserDetailsService {

    private UserDAO dao;

    private JwtUtility jwtUtility;

    private OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    private SesAdapter sesAdapter;

    private static final Set<String> allowedEmailDomains = ImmutableSet.of("brown.edu");

    @Autowired
    public UserService(UserDAO dao,
                       JwtUtility jwtUtility,
                       OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver,
                       SesAdapter sesAdapter) {
        this.dao = dao;
        this.jwtUtility = jwtUtility;
        this.oAuth2AuthorizationRequestResolver = oAuth2AuthorizationRequestResolver;
        this.sesAdapter = sesAdapter;
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
    public synchronized int createNewUser(User user) {
        try {
            String username = user.getUsername();
            dao.getUserByUsername(username);

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

    public synchronized void updateUser(User newRecord) throws UserNotFoundException {
        // bubble up exception if user not found
        User oldRecord = dao.getUserByUsername(newRecord.getUsername());

        // these fields cannot be modified by users, setting to null retains the old value
        newRecord.setId(oldRecord.getId());
        newRecord.setAuthorities(null);
        newRecord.setVerified(oldRecord.getVerified());
        newRecord.setCreationType(null);
        newRecord.setProfileCompleted(checkProfileComplete(newRecord) || oldRecord.getProfileCompleted());
        newRecord.setCreatedAt(null);
        if (UserCreationType.O_AUTH.equals(oldRecord.getCreationType())) {
            newRecord.setPassword(null);
        }

        dao.saveUser(newRecord);
    }

    /**
     * Deletes a user.
     * <p>
     * Returns one of the following exit codes:
     * <ul>
     * <li> 0 indicates successful deletion </li>
     * <li> -1 indicates username does not exist </li>
     * <li> -2 indicates failure to delete </li>
     * <li> -3 indicates unexpected exception occurred </li>
     * </ul>
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

        if (!UserCreationType.TRADITIONAL.equals(user.getCreationType())) {
            throw new UnmatchedUserCreationTypeException(UserCreationType.TRADITIONAL);
        }

        if (!user.getVerified()) {
            return "notVerified";
        }

        return jwtUtility.generateToken(user);
    }

    public String generateOAuthJWT(String username) {
        User user;

        try {
            user = loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            // new user, create new record;
            user = User.builder()
                    .username(username)
                    .creationType(UserCreationType.O_AUTH)
                    .verified(true)
                    .profileCompleted(false)
                    .createdAt(new Date())
                    .build();

            createNewUser(user);
        }

        if (!UserCreationType.O_AUTH.equals(user.getCreationType())) {
            throw new UnmatchedUserCreationTypeException(UserCreationType.O_AUTH);
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

    public void authorizeEmailDomain(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress)) {
            throw new DisallowedEmailDomainException("Empty email address", emailAddress);
        }

        String[] splits = emailAddress.split("@");
        if (splits.length >= 2 && allowedEmailDomains.contains(splits[1])) {
            return;
        }

        throw new DisallowedEmailDomainException("Disallowed email domain", emailAddress);
    }

    public String startEmailVerification(String emailAddress) {
        User codeModel = User.builder()
                .username(emailAddress + "verify")
                .build();
        String verificationCode = jwtUtility.generateToken(codeModel);

        dao.setEmailVerificationCode(emailAddress, verificationCode);

        sesAdapter.sendAccountVerificationEmail(emailAddress, verificationCode);

        return verificationCode;
    }

    public boolean validateEmailVerificationCode(String emailAddress, String inputCode) {
        String expectedCode = dao.getEmailVerificationCode(emailAddress);

        if (StringUtils.isEmpty(inputCode) || !inputCode.equals(expectedCode)) {
            log.info("Email Verification: Code {} did not match with the expected code {} for user {}", inputCode,
                    expectedCode, emailAddress);
            return false;
        }

        log.info("Email Verification: Code {} was validated for user {}", inputCode, emailAddress);
        return true;
    }

    public void verifyUser(String username) throws UserNotFoundException {
        User user = dao.getUserByUsername(username);
        if (user.getVerified()) {
            log.info("User {} successfully verified their email more than once, something might be wrong. " +
                    "Check UserController/Service/DAO", username);
        }

        // verify and delete entry from db
        user.setVerified(true);
        dao.saveUser(user);
        dao.setEmailVerificationCode(username, null);
    }

    public boolean checkProfileComplete(User newRecord) {
        return StringUtils.isNotBlank(newRecord.getUsername())
                && StringUtils.isNotBlank(newRecord.getFirstName()) && StringUtils.isNotBlank(newRecord.getLastName())
                && StringUtils.isNotBlank(newRecord.getClassYear())
                && !CollectionUtils.isEmpty(newRecord.getMajors()) && StringUtils.isNotBlank(newRecord.getMajors().get(0))
                && newRecord.getInterests1() != null && newRecord.getInterests1().size() == 3
                && newRecord.getInterests2() != null && newRecord.getInterests2().size() == 3
                && newRecord.getInterests3() != null && newRecord.getInterests3().size() == 3;
    }
}

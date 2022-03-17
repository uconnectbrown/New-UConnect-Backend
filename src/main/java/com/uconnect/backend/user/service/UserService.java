package com.uconnect.backend.user.service;

import com.google.common.collect.ImmutableSet;
import com.uconnect.backend.awsadapter.SesAdapter;
import com.uconnect.backend.exception.ConcentrationNotFoundException;
import com.uconnect.backend.exception.CourseNotFoundException;
import com.uconnect.backend.exception.DisallowedEmailDomainException;
import com.uconnect.backend.exception.UnknownOAuthRegistrationException;
import com.uconnect.backend.exception.UnmatchedUserCreationTypeException;
import com.uconnect.backend.exception.UserNotFoundException;
import com.uconnect.backend.search.dao.SearchDAO;
import com.uconnect.backend.search.model.Concentration;
import com.uconnect.backend.search.model.CourseRoster;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.dao.UserDAO;
import com.uconnect.backend.user.model.Course;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@NoArgsConstructor
public class UserService implements UserDetailsService {

    private UserDAO dao;

    private SearchDAO searchDAO;

    private JwtUtility jwtUtility;

    private OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver;

    private SesAdapter sesAdapter;

    private static final Set<String> allowedEmailDomains =
            ImmutableSet.of("brown.edu");

    @Autowired
    public UserService(UserDAO dao,
            SearchDAO searchDAO,
            JwtUtility jwtUtility,
            OAuth2AuthorizationRequestResolver oAuth2AuthorizationRequestResolver,
            SesAdapter sesAdapter) {
        this.dao = dao;
        this.searchDAO = searchDAO;
        this.jwtUtility = jwtUtility;
        this.oAuth2AuthorizationRequestResolver =
                oAuth2AuthorizationRequestResolver;
        this.sesAdapter = sesAdapter;
    }

    @Override
    public User loadUserByUsername(String username) {
        try {
            return dao.getUserByUsername(username);
        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException(
                    String.format("%s is not a valid username", username));
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
            populateTablesForNewUser(user);

            // successfully created a new user
            return 0;
        } catch (Exception e) {
            log.error("Unexpected exception while creating new user "
                    + user.getUsername() + ": {}", e);
            return -2;
        }
    }

    private synchronized void populateTablesForNewUser(User user) {
        try {
            // Populate user table
            dao.saveUser(user);

            // Populate course table
            Set<Course> courses = (user.getCourses() == null ? new HashSet<>()
                    : user.getCourses());
            for (Course c : courses) {
                CourseRoster courseRoster =
                        CourseRoster.builder().name(c.getName()).build();
                searchDAO.createCourseRosterIfNotExists(courseRoster);
                searchDAO.addUserToCourseRoster(user.getUsername(),
                        c.getName());
            }

            // Populate concentration table
            List<String> concentrations =
                    (user.getMajors() == null ? new ArrayList<>()
                            : user.getMajors());
            for (String c : concentrations) {
                Concentration concentration =
                        Concentration.builder().name(c).build();
                searchDAO.createConcentrationIfNotExists(concentration);
                searchDAO.addUserToConcentration(user.getUsername(), c);
            }
        } catch (CourseNotFoundException | ConcentrationNotFoundException e) {
            // to suppress compiler warnings. should never get here.
            return;
        }
    }

    public synchronized void updateUser(User newRecord)
            throws UserNotFoundException {
        // bubble up exception if user not found
        User oldRecord = dao.getUserByUsername(newRecord.getUsername());

        // these fields cannot be modified by users, setting to null retains the
        // old value
        newRecord.setId(oldRecord.getId());
        newRecord.setAuthorities(null);
        newRecord.setVerified(oldRecord.isVerified());
        newRecord.setCreationType(null);
        newRecord.setProfileCompleted(checkProfileComplete(newRecord)
                || oldRecord.isProfileCompleted());
        newRecord.setCreatedAt(null);
        if (UserCreationType.O_AUTH.equals(oldRecord.getCreationType())) {
            newRecord.setPassword(null);
        }

        updateTablesForExistingUser(oldRecord, newRecord);
    }

    private synchronized void updateTablesForExistingUser(User oldRecord,
            User newRecord) {
        try {
            dao.saveUser(newRecord);
            updateCourseTable(oldRecord, newRecord);
            updateConcentrationTable(oldRecord, newRecord);
        } catch (CourseNotFoundException | ConcentrationNotFoundException e) {
            // to suppress compiler warnings. should never get here.
            return;
        }
    }

    private synchronized void updateCourseTable(User oldRecord, User newRecord)
            throws CourseNotFoundException {
        Set<Course> coursesToRemove =
                difference(oldRecord.getCourses(), newRecord.getCourses());
        for (Course c : coursesToRemove) {
            CourseRoster courseRoster =
                    CourseRoster.builder().name(c.getName()).build();
            searchDAO.createCourseRosterIfNotExists(courseRoster);
            searchDAO.removeUserFromCourseRoster(newRecord.getUsername(),
                    c.getName());
        }
        Set<Course> coursesToAdd =
                difference(newRecord.getCourses(), oldRecord.getCourses());
        for (Course c : coursesToAdd) {
            CourseRoster courseRoster =
                    CourseRoster.builder().name(c.getName()).build();
            searchDAO.createCourseRosterIfNotExists(courseRoster);
            searchDAO.addUserToCourseRoster(newRecord.getUsername(),
                    c.getName());
        }
    }

    private synchronized void updateConcentrationTable(User oldRecord,
            User newRecord) throws ConcentrationNotFoundException {
        List<String> concentrationsToRemove =
                difference(oldRecord.getMajors(), newRecord.getMajors());
        for (String c : concentrationsToRemove) {
            Concentration concentration =
                    Concentration.builder().name(c).build();
            searchDAO.createConcentrationIfNotExists(concentration);
            searchDAO.removeUserFromConcentration(newRecord.getUsername(), c);
        }
        List<String> concentrationsToAdd =
                difference(newRecord.getMajors(), oldRecord.getMajors());
        for (String c : concentrationsToAdd) {
            Concentration concentration =
                    Concentration.builder().name(c).build();
            searchDAO.createConcentrationIfNotExists(concentration);
            searchDAO.addUserToConcentration(newRecord.getUsername(), c);
        }
    }

    // Returns difference of sets (a/b) as a new set.
    private synchronized <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> aCopy = (a == null ? new HashSet<>() : new HashSet<>(a));
        Set<T> bCopy = (b == null ? new HashSet<>() : new HashSet<>(b));
        aCopy.removeAll(bCopy);
        return aCopy;
    }

    // Above function overloaded for lists.
    private synchronized <T> List<T> difference(List<T> a, List<T> b) {
        List<T> aCopy = (a == null ? new ArrayList<>() : new ArrayList<>(a));
        List<T> bCopy = (b == null ? new ArrayList<>() : new ArrayList<>(b));
        aCopy.removeAll(bCopy);
        return aCopy;
    }

    /**
     * Deletes a user.
     * <p>
     * Returns one of the following exit codes:
     * <ul>
     * <li>0 indicates successful deletion</li>
     * <li>-1 indicates username does not exist</li>
     * <li>-2 indicates failure to delete</li>
     * <li>-3 indicates unexpected exception occurred</li>
     * </ul>
     *
     * @param username The username of the user to delete
     * @return An exit code
     */
    public int deleteUser(String username) {
        try {
            return dao.deleteUser(username);
        } catch (Exception e) {
            log.error("Unexpected exception while deleting user " + username
                    + ": {}", e);
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
            throw new UnmatchedUserCreationTypeException(
                    UserCreationType.TRADITIONAL);
        }

        if (!user.isVerified()) {
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
                    .createdAt(new Date())
                    .build();

            createNewUser(user);
        }

        if (!UserCreationType.O_AUTH.equals(user.getCreationType())) {
            throw new UnmatchedUserCreationTypeException(
                    UserCreationType.O_AUTH);
        }

        return jwtUtility.generateToken(user);
    }

    public OAuth2LoginAuthenticationToken getOAuth2LoginAuthenticationToken(
            HttpServletRequest request, String authCode,
            String registrationId, ClientRegistration registration) {
        if (registration == null) {
            throw new UnknownOAuthRegistrationException();
        }

        OAuth2AuthorizationRequest authorizationRequest =
                oAuth2AuthorizationRequestResolver.resolve(request,
                        registrationId);
        OAuth2AuthorizationResponse authorizationResponse =
                OAuth2AuthorizationResponse
                        .success(authCode)
                        .redirectUri("/")
                        .state(authorizationRequest.getState())
                        .build();

        return new OAuth2LoginAuthenticationToken(registration,
                new OAuth2AuthorizationExchange(authorizationRequest,
                        authorizationResponse));
    }

    public void authorizeEmailDomain(String emailAddress) {
        if (StringUtils.isEmpty(emailAddress)) {
            throw new DisallowedEmailDomainException("Empty email address",
                    emailAddress);
        }

        String[] splits = emailAddress.split("@");
        if (splits.length >= 2 && allowedEmailDomains.contains(splits[1])) {
            return;
        }

        throw new DisallowedEmailDomainException("Disallowed email domain",
                emailAddress);
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

    public boolean validateEmailVerificationCode(String emailAddress,
            String inputCode) {
        String expectedCode = dao.getEmailVerificationCode(emailAddress);

        if (StringUtils.isEmpty(inputCode) || !inputCode.equals(expectedCode)) {
            log.info(
                    "Email Verification: Code {} did not match with the expected code {} for user {}",
                    inputCode,
                    expectedCode, emailAddress);
            return false;
        }

        log.info("Email Verification: Code {} was validated for user {}",
                inputCode, emailAddress);
        return true;
    }

    public void verifyUser(String username) throws UserNotFoundException {
        User user = dao.getUserByUsername(username);
        if (user.isVerified()) {
            log.info(
                    "User {} successfully verified their email more than once, something might be wrong. "
                            +
                            "Check UserController/Service/DAO",
                    username);
        }

        // verify and delete entry from db
        user.setVerified(true);
        dao.saveUser(user);
        dao.setEmailVerificationCode(username, null);
    }

    public boolean checkProfileComplete(User newRecord) {
        return StringUtils.isNotBlank(newRecord.getUsername())
                && StringUtils.isNotBlank(newRecord.getFirstName())
                && StringUtils.isNotBlank(newRecord.getLastName())
                && StringUtils.isNotBlank(newRecord.getClassYear())
                && !CollectionUtils.isEmpty(newRecord.getMajors())
                && StringUtils.isNotBlank(newRecord.getMajors().get(0))
                && newRecord.getInterests1() != null
                && newRecord.getInterests1().size() == 3
                && newRecord.getInterests2() != null
                && newRecord.getInterests2().size() == 3
                && newRecord.getInterests3() != null
                && newRecord.getInterests3().size() == 3;
    }
}

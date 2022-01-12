package com.uconnect.backend.security.util;

import com.uconnect.backend.exception.UnauthorizedUserRequestException;
import com.uconnect.backend.security.jwt.util.RequestPermissionUtility;
import com.uconnect.backend.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class RequestPermissionUtilityTest {
    private RequestPermissionUtility requestPermissionUtility;

    private final String authenticatedUsername = "mike's@givemethebuzz.hockey";
    private User authenticatedUser;

    @BeforeEach
    public void setUp() {
        requestPermissionUtility = new RequestPermissionUtility();

        authenticatedUser = User.builder().username(authenticatedUsername).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(authenticatedUser, null));
    }

    @AfterEach
    public void teardown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testAuthorizeUser() {
        String requestedUsername = "chewy@gum.cpu";

        assertThrows(UnauthorizedUserRequestException.class, () ->
                requestPermissionUtility.authorizeUser(requestedUsername));
    }
}

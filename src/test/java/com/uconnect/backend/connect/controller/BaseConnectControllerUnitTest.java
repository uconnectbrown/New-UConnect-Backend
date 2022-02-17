package com.uconnect.backend.connect.controller;

import com.uconnect.backend.connect.service.ConnectService;
import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.security.RequestPermissionUtility;
import com.uconnect.backend.user.service.UserService;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.authentication.OidcAuthorizationCodeAuthenticationProvider;

/**
 * Base class for all ConnectController unit tests.
 */
public class BaseConnectControllerUnitTest extends BaseUnitTest {
    @MockBean
    public ConnectService connectService;

    @MockBean
    private UserService userService;

    @MockBean
    public RequestPermissionUtility requestPermissionUtility;

    @MockBean
    public PasswordEncoder passwordEncoder;

    @MockBean
    public AuthenticationProvider authenticationProvider;

    @MockBean
    public OidcAuthorizationCodeAuthenticationProvider oidcAuthorizationCodeAuthenticationProvider;
}

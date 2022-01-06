package com.uconnect.backend.user.controller;

import com.uconnect.backend.helper.BaseUnitTest;
import com.uconnect.backend.user.service.UserService;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Base class for all Spring Boot unit tests, used to populate application context with necessary mock beans
 */
public class BaseUserControllerUnitTest extends BaseUnitTest {
    @MockBean
    private UserService userService;

    @MockBean
    public PasswordEncoder passwordEncoder;

    @MockBean
    public AuthenticationProvider authenticationProvider;
}

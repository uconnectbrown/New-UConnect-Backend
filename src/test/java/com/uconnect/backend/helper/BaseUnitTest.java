package com.uconnect.backend.helper;

import com.uconnect.backend.security.jwt.filter.JwtFilter;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Base class for all Spring Boot unit tests, used to populate application context with necessary mock beans
 */
public class BaseUnitTest {
    @MockBean
    public JwtFilter jwtFilter;

    @MockBean
    public JwtUtility jwtUtility;
}

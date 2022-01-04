package com.uconnect.backend.security.filter;

import com.uconnect.backend.security.jwt.filter.JwtFilter;
import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {
    @Mock
    private JwtUtility jwtUtility;

    @Mock
    private UserService userService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private JwtFilter filter;

    private final String authHeaderName = "Authorization";

    private final String usernameHeaderName = "Username";

    private final String goodToken = "Bearer 123456";

    private final String invalidToken = "Bearer 654321";

    private final String malformedToken = "Bearer";

    private final String username = "sample@email.com";

    private User user;

    @BeforeEach
    public void setup() {
        filter = new JwtFilter(jwtUtility, userService);

        user = User.builder().username(username).build();
    }

    @AfterEach
    public void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testRequestNoToken() throws ServletException, IOException {
        when(request.getHeader(authHeaderName)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testRequestNoUsername() throws ServletException, IOException {
        when(request.getHeader(authHeaderName)).thenReturn(goodToken);
        when(request.getHeader(usernameHeaderName)).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testMalformedToken() throws ServletException, IOException {
        when(request.getHeader(authHeaderName)).thenReturn(malformedToken);
        when(request.getHeader(usernameHeaderName)).thenReturn(username);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testInvalidToken() throws ServletException, IOException {
        when(request.getHeader(authHeaderName)).thenReturn(invalidToken);
        when(request.getHeader(usernameHeaderName)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(jwtUtility.validateToken(invalidToken.split(" ")[1], user)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testValidToken() throws ServletException, IOException {
        when(request.getHeader(authHeaderName)).thenReturn(goodToken);
        when(request.getHeader(usernameHeaderName)).thenReturn(username);
        when(userService.loadUserByUsername(username)).thenReturn(user);
        when(jwtUtility.validateToken(goodToken.split(" ")[1], user)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        assertEquals(SecurityContextHolder.getContext().getAuthentication().getPrincipal(), user);
    }
}

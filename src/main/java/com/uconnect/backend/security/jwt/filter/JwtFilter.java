package com.uconnect.backend.security.jwt.filter;

import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.model.User;
import com.uconnect.backend.user.service.UserService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;

    private final UserService userService;

    public JwtFilter(JwtUtility jwtUtility, UserService userService) {
        this.jwtUtility = jwtUtility;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        String username = request.getHeader("Username");
        String token = null;

        if (auth != null) {
            token = auth.split(" ")[1];
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            User user = userService.loadUserByUsername(username);

            if (jwtUtility.validateToken(token, user)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                        = new UsernamePasswordAuthenticationToken(user, null,
                        user.getAuthorities());

                usernamePasswordAuthenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}


package com.uconnect.backend.security.util;

import com.uconnect.backend.security.jwt.util.JwtUtility;
import com.uconnect.backend.user.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtUtilityTest {
    private JwtUtility jwtUtility;

    String subject = "testSub";
    String id = "0";
    String secretKey = System.getenv("JWT_SECRET");
    Map<String, Object> claims = new HashMap<>();

    @BeforeEach
    public void setUp() {
        jwtUtility = new JwtUtility();

        claims.put(JwtUtility.JWT_CLAIM_ID, id);
    }

    @Test
    public void testGenerateToken() {
        // have to do the loops due to issues with time precision
        boolean areEventuallyEqual = false;
        for (int i = 0; i < 20; i++) {
            long iss = System.currentTimeMillis();
            long exp = iss + JwtUtility.JWT_TOKEN_VALIDITY * 1000;
            String expectedToken = generateTestToken(claims, subject, iss, exp, secretKey);

            User user = User.builder()
                    .username(subject)
                    .id(id)
                    .build();
            String testToken = jwtUtility.generateToken(user);

            if (expectedToken.equals(testToken)) {
                areEventuallyEqual = true;
                break;
            }
        }

        assertTrue(areEventuallyEqual);
    }

    @Test
    public void testValidateTokenValid() {
        long iss = System.currentTimeMillis();
        long exp = iss + JwtUtility.JWT_TOKEN_VALIDITY * 1000;
        String validToken = generateTestToken(claims, subject, iss, exp, secretKey);

        User user = User.builder()
                .username(subject)
                .id(id)
                .build();

        assertTrue(jwtUtility.validateToken(validToken, user));
    }

    @Test
    public void testValidateTokenExpired() {
        long iss = System.currentTimeMillis() - 50_000;
        long exp = iss + 100;
        String expiredToken = generateTestToken(claims, subject, iss, exp, secretKey);

        User user = User.builder()
                .username(subject)
                .id(id)
                .build();

        assertFalse(jwtUtility.validateToken(expiredToken, user));
    }

    @Test
    public void testValidateTokenIncorrectUsername() {
        long iss = System.currentTimeMillis();
        long exp = iss + JwtUtility.JWT_TOKEN_VALIDITY * 1000;

        String incorrectUsername = "goop";
        String incorrectUsernameToken = generateTestToken(claims, incorrectUsername, iss, exp, secretKey);

        User user = User.builder()
                .username(subject)
                .id(id)
                .build();

        assertFalse(jwtUtility.validateToken(incorrectUsernameToken, user));
    }

    @Test
    public void testValidateTokenInvalidSignature() {
        long iss = System.currentTimeMillis();
        long exp = iss + JwtUtility.JWT_TOKEN_VALIDITY * 1000;

        String invalidSecretKey = "didWeJustBecomeBestFriends";
        String validToken = generateTestToken(claims, subject, iss, exp, invalidSecretKey);

        User user = User.builder()
                .username(subject)
                .id(id)
                .build();

        assertFalse(jwtUtility.validateToken(validToken, user));
    }

    private String generateTestToken(Map<String, Object> claims, String subject, long iss, long exp, String secretKey) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(iss))
                .setExpiration(new Date(exp))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }
}

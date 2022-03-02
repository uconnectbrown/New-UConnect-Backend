package com.uconnect.backend.security.jwt.util;

import com.uconnect.backend.user.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtility implements Serializable {

    private static final long serialVersionUID = 234234523523L;

    // TODO: decide token expiration time (current: 1 hours)
    public static final long JWT_TOKEN_VALIDITY = 1 * 60 * 60;
    // sub = username
    public static final String JWT_CLAIM_ID = "id";

    private final String secretKey = System.getenv("JWT_SECRET");

    //retrieve username from jwt token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieving any information from token we will need the secret key
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
    }

    //generate token for user
    public String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JWT_CLAIM_ID, userDetails.getId());

        return doGenerateToken(claims, userDetails.getUsername());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        long iss = System.currentTimeMillis();
        long exp = iss + JWT_TOKEN_VALIDITY * 1000;

        return Jwts.builder().setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(iss))
                .setExpiration(new Date(exp))
                .signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

    //validate token
    public boolean validateToken(String token, User user) {
        try {
            final String username = getUsernameFromToken(token);

            if (username == null || !username.equals(user.getUsername())) {
                log.info("JWT token \n\"{}\"\nfailed to validate for user \"{}\". \nUsername in token: \"{}\"", token, user.getUsername(),
                        username);
                return false;
            }

            return true;
        } catch (Exception e) {
            if (user == null || user.getUsername() == null) {
                log.info("JWT token \n\"{}\"\nfailed to validate. \nNo username provided. \nException: {}", token,
                        e.getMessage());
            } else {
                log.info("JWT token \n\"{}\"\nfailed to validate for user \"{}\". \nException: {}", token, user.getUsername(),
                        e.getMessage());
            }
            return false;
        }
    }
}
package com.uconnect.backend.security.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthJwtResponse {
    private String jwtToken;
    private String username;
}

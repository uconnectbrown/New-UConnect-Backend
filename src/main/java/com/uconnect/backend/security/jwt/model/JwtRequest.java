package com.uconnect.backend.security.jwt.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequest {
    @Email(message = "Username is not a valid email address")
    private String username;

    @Size(min = 8, max = 32, message = "Password length must be between 8 and 32 characters (inclusive)")
    private String password;
}

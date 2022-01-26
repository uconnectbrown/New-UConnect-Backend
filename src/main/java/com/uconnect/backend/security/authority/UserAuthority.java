package com.uconnect.backend.security.authority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserAuthority implements GrantedAuthority {
    private String role;

    @Override
    public String getAuthority() {
        return role;
    }
}

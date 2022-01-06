package com.uconnect.backend.security.oauth;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class OAuthRequest {
    @NotNull
    private String authCode;
}

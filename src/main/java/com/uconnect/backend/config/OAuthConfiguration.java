package com.uconnect.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthorizationCodeAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

@Configuration
public class OAuthConfiguration {
    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> getOAuth2AccessTokenResponseClient() {
        return new DefaultAuthorizationCodeTokenResponseClient();
    }

    @Bean
    public AuthenticationProvider getAuthenticationProvider(
            OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenResponseClient) {
        return new OAuth2AuthorizationCodeAuthenticationProvider(tokenResponseClient);
    }

    @Bean
    public OAuth2AuthorizationRequestResolver getOAuth2AuthorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        return new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository,
                "/v1/user/authenticate/oauth/");
    }
}

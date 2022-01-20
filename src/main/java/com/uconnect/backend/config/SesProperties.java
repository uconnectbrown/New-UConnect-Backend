package com.uconnect.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "ses")
@Configuration
@Setter
public class SesProperties {
    private String sesFromAddress;

    @Bean (name = "sesFromAddress")
    public String getSesFromAddress() {
        return sesFromAddress;
    }
}

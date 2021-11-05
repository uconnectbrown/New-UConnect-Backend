package com.uconnect.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "db-setting")
@Configuration
@Setter
public class DbProperties {
    private String localDbPort;

    @Bean(name = "localDbPort")
    public String getLocalDbPort() {
        return localDbPort;
    }
}

package com.uconnect.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "db-table-name")
@Configuration
@Setter
public class DbTableProperties {
    private String user;

    @Bean (name = "userTableName")
    public String getUserTableName() {
        return user;
    }
}

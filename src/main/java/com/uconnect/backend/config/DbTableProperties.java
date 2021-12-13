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

    private String emailIndex;

    @Bean (name = "userTableName")
    public String getUserTableName() {
        return user;
    }

    @Bean (name = "emailIndexName")
    public String getEmailIndexName() {
        return emailIndex;
    }
}

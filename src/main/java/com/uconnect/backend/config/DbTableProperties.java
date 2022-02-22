package com.uconnect.backend.config;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "db-table-name")
@Configuration
@Setter
public class DbTableProperties {
    // user table
    private String user;
    // user table index
    private String emailIndex;

    // email verification table
    private String emailVerification;

    // event board tables
    private String eventBoardEventHidden;
    private String eventBoardEventPublished;
    private String eventBoardCommentHidden;
    private String eventBoardCommentPublished;
    // event board indices (names shared by hidden and published tables)
    private String eventBoardAuthorIndex;
    private String eventBoardHostIndex;
    private String eventBoardIndexIndex;
    private String eventBoardTitleIndex;

    // counter table
    private String counter;

    @Bean(name = "userTableName")
    public String getUserTableName() {
        return user;
    }

    @Bean(name = "emailIndexName")
    public String getEmailIndexName() {
        return emailIndex;
    }

    @Bean(name = "emailVerificationTableName")
    public String getEmailVerificationTableName() {
        return emailVerification;
    }

    @Bean(name = "eventBoardEventHiddenTableName")
    public String getEventBoardEventHidden() {
        return eventBoardEventHidden;
    }

    @Bean(name = "eventBoardEventPublishedTableName")
    public String getEventBoardEventPublished() {
        return eventBoardEventPublished;
    }

    @Bean(name = "eventBoardCommentHiddenTableName")
    public String getEventBoardCommentHidden() {
        return eventBoardCommentHidden;
    }

    @Bean(name = "eventBoardCommentPublishedTableName")
    public String getEventBoardCommentPublished() {
        return eventBoardCommentPublished;
    }

    @Bean(name = "eventBoardAuthorIndexName")
    public String getEventBoardAuthorIndex() {
        return eventBoardAuthorIndex;
    }

    @Bean(name = "eventBoardHostIndexName")
    public String getEventBoardHostIndex() {
        return eventBoardHostIndex;
    }

    @Bean(name = "eventBoardIndexIndexName")
    public String getEventBoardIndexIndex() {
        return eventBoardIndexIndex;
    }

    @Bean(name = "eventBoardTitleIndexName")
    public String getEventBoardTitleIndex() {
        return eventBoardTitleIndex;
    }

    @Bean(name = "counterTableName")
    public String getCounter() {
        return counter;
    }
}

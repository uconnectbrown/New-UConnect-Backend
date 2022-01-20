package com.uconnect.backend.awsadapter;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AwsConfiguration {
    @Autowired
    private String localDbPort;

    @Bean
    public AWSCredentialsProvider getAWSCredentialsProvider() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    @Bean
    public AmazonDynamoDB getAmazonDynamoDB(AWSCredentialsProvider awsCredentialsProvider) throws Exception {
        boolean isDev = "dev".equals(System.getenv("SPRING_PROFILES_ACTIVE"));
        boolean isManualTesting = "true".equals(System.getenv("IS_MANUAL_TESTING"));

        if (isDev) {
            if (isManualTesting) {
                LocalDdbServerRunner runner = new LocalDdbServerRunner(localDbPort);
                runner.start();
            }

            return AmazonDynamoDBClientBuilder
                    .standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder
                                    .EndpointConfiguration(
                                    "http://localhost:" + localDbPort,
                                    "us-east-1"))
                    .build();
        }

        return AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion("us-east-1")
                .build();
    }

    @Bean
    public AmazonSimpleEmailService getAmazonSes(AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonSimpleEmailServiceClientBuilder
                .standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion("us-east-1")
                .build();
    }
}

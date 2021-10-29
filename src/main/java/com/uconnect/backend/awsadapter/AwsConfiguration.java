package com.uconnect.backend.awsadapter;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.uconnect.backend.awsadapter.DdbAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.uconnect.backend")
@Slf4j
public class AwsConfiguration {
    @Bean
    public AWSCredentialsProvider getAWSCredentialsProvider() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    @Bean
    public AmazonDynamoDB getAmazonDynamoDB(AWSCredentialsProvider awsCredentialsProvider) {
        return AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(awsCredentialsProvider)
                .withRegion("us-east-1")
                .build();
    }

    @Bean
    public DdbAdapter getDdbAdapter(AmazonDynamoDB dynamoDB) {
        return new DdbAdapter(dynamoDB);
    }
}

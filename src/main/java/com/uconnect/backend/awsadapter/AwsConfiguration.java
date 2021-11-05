package com.uconnect.backend.awsadapter;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.uconnect.backend")
@Slf4j
public class AwsConfiguration {
    @Value("${amazon.dynamodb.localdbport")
    private String localDbPort;

    @Bean
    public AWSCredentialsProvider getAWSCredentialsProvider() {
        return DefaultAWSCredentialsProviderChain.getInstance();
    }

    @Bean
    public AmazonDynamoDB getAmazonDynamoDB(AWSCredentialsProvider awsCredentialsProvider) throws Exception {
        boolean isDev = "DEV".equals(System.getenv("spring_profiles_active"));

        if (isDev) {
            LocalDdbServerRunner localDdbServerRunner = new LocalDdbServerRunner();
            localDdbServerRunner.start();

            return AmazonDynamoDBClientBuilder
                    .standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder
                                    .EndpointConfiguration(
                                    "http://localhost:" + localDbPort, "us-east-1"))
                    .build();
        }

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

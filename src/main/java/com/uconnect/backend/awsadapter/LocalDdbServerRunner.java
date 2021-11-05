package com.uconnect.backend.awsadapter;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class LocalDdbServerRunner {
    @Value("${amazon.dynamodb.localdbport")
    private String localDbPort;
    private DynamoDBProxyServer server;

    public LocalDdbServerRunner() {
        System.setProperty("sqlite4java.library.path", "sqlite4java-native-libs");
    }

    public void start() throws Exception {
        log.info(localDbPort);
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-inMemory", "-port", localDbPort, "delayTransientStatuses"});
        server.start();
        log.info(String.format("DynamoDB Local has started on port %s", localDbPort));
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

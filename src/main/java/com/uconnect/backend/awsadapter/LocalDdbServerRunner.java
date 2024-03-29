package com.uconnect.backend.awsadapter;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class LocalDdbServerRunner {
    private final String localDbPort;
    private DynamoDBProxyServer server;

    @Autowired
    public LocalDdbServerRunner(String localDbPort) {
        this.localDbPort = localDbPort;
        System.setProperty("sqlite4java.library.path", "sqlite4java-native-libs");
    }

    public void start() throws Exception {
        server = ServerRunner.createServerFromCommandLineArgs(
                new String[]{"-sharedDb", "-inMemory", "-port", localDbPort});
        server.start();
        log.info(String.format("DynamoDB Local has started on port %s", localDbPort));
    }

    public void stop() {
        try {
            server.stop();
            log.info("DynamoDB Local has stopped");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

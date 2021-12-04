package com.uconnect.backend.awsadapter;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalDdbServerRunner {
    private final String localDbPort;
    private DynamoDBProxyServer server;

    public LocalDdbServerRunner(String localDbPort) {
        this.localDbPort = localDbPort;
        System.setProperty("sqlite4java.library.path", "sqlite4java-native-libs");
    }

    public void start() throws Exception {
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

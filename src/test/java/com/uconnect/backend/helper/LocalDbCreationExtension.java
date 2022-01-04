package com.uconnect.backend.helper;

import com.uconnect.backend.awsadapter.LocalDdbServerRunner;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class LocalDbCreationExtension implements BeforeAllCallback, AfterAllCallback {
    private final LocalDdbServerRunner runner;

    private final String localDbPort = "5000";

    public LocalDbCreationExtension() {
        runner = new LocalDdbServerRunner(localDbPort);
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        runner.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        runner.stop();
    }
}

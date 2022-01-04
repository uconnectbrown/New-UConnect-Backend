package com.uconnect.backend.helper;

import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Base class for all integration tests
 */
public class BaseIntTest {
    @RegisterExtension
    public static LocalDbCreationExtension localDbCreationExtension = new LocalDbCreationExtension();
}

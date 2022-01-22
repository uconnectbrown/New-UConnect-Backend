package com.uconnect.backend.helper;

import com.uconnect.backend.user.model.User;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * Very rudimentary mock data generator for lack of a better one.
 */
public class MockData {
    /**
     * Generate a single User with valid fields.
     *
     * @return A randomly generated User with an {@literal@}brown.edu username.
     */
    public static User generateValidUser() {
        String email = randomAlphanumeric(7) + "@brown.edu";
        return User.builder()
                .username(email)
                .password(randomAlphanumeric(15))
                .firstName(randomAlphanumeric(7))
                .lastName(randomAlphanumeric(7))
                .build();
    }
}

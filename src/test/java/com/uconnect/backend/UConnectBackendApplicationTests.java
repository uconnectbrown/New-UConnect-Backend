package com.uconnect.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class UConnectBackendApplicationTests {

    @Test
    void itWorks() {
        System.out.println("UConnect serverside APIs are now available");
    }
}

package com.uconnect.backend.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.uconnect.backend")
public class UConnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UConnectBackendApplication.class, args);
    }

}
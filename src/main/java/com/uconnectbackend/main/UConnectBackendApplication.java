package com.uconnectbackend.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.uconnectbackend")
public class UConnectBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(UConnectBackendApplication.class, args);
    }

}
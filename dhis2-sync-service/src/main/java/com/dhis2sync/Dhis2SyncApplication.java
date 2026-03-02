package com.dhis2sync;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the DHIS2 Synchronisation Service.
 * Scheduling is enabled so that @Scheduled beans are processed.
 */
@SpringBootApplication
@EnableScheduling
public class Dhis2SyncApplication {

    public static void main(String[] args) {
        SpringApplication.run(Dhis2SyncApplication.class, args);
    }
}
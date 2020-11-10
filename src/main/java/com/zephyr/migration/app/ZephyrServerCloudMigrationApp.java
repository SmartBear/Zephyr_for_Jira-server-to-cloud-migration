package com.zephyr.migration.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.zephyr"})
public class ZephyrServerCloudMigrationApp {
    public static void main(String[] args) {
        SpringApplication.run(ZephyrServerCloudMigrationApp.class, args);
    }
}

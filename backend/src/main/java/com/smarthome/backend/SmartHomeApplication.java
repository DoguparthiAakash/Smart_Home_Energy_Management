package com.smarthome.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.smarthome.backend.model")
@EnableJpaRepositories(basePackages = "com.smarthome.backend.repository")
public class SmartHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartHomeApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner commandLineRunner(
            org.springframework.context.ApplicationContext ctx) {
        return args -> {
            System.out.println("\n============================================================");
            System.out.println("SMART HOME ENERGY MANAGER STARTED SUCCESSFULLY");
            System.out.println("Access URL: http://localhost:8080");
            System.out.println("Admin User: muterornament");
            System.out.println("Guest User: guest");
            System.out.println("============================================================\n");
        };
    }

}

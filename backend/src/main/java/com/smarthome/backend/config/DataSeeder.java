package com.smarthome.backend.config;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository, DeviceRepository deviceRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                // Create Admin User
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@smarthome.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(User.Role.ADMIN);
                userRepository.save(admin);

                // Create Regular User
                User user = new User();
                user.setName("Home Owner");
                user.setEmail("user@smarthome.com");
                user.setPassword(passwordEncoder.encode("password"));
                user.setRole(User.Role.HOMEOWNER);
                User savedUser = userRepository.save(user);

                // Create Devices for Homeowner
                Device d1 = new Device();
                d1.setName("Living Room AC");
                d1.setType("AC");
                d1.setPowerRating(1500.0);
                d1.setStatus(false);
                d1.setUser(savedUser);

                Device d2 = new Device();
                d2.setName("Smart Fridge");
                d2.setType("FRIDGE");
                d2.setPowerRating(200.0);
                d2.setStatus(true);
                d2.setUser(savedUser);

                Device d3 = new Device();
                d3.setName("Bedroom Light");
                d3.setType("LIGHT");
                d3.setPowerRating(15.0);
                d3.setStatus(false);
                d3.setUser(savedUser);

                Device d4 = new Device();
                d4.setName("Water Heater");
                d4.setType("HEATER");
                d4.setPowerRating(2000.0);
                d4.setStatus(false);
                d4.setUser(savedUser);

                deviceRepository.saveAll(Arrays.asList(d1, d2, d3, d4));
                System.out.println("Data Seeding Completed: Created 2 users and 4 devices.");
            }
        };
    }
}

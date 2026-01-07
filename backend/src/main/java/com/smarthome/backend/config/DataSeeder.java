package com.smarthome.backend.config;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.service.TotpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private TotpService totpService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            // Create uploads directory
            Files.createDirectories(Paths.get("uploads"));

            if (userRepository.count() == 0) {
                // Admin with TOTP
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@smarthome.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setRole(User.Role.ADMIN);

                // Generate Secret
                String secret = totpService.generateSecret().getKey();
                admin.setTwoFactorSecret(secret);

                userRepository.save(admin);

                System.out.println("==========================================");
                System.out.println("ADMIN ACCOUNT CREATED");
                System.out.println("Email: admin@smarthome.com");
                System.out.println("Password: password");
                System.out.println("TOTP SECRET: " + secret);
                System.out.println("==========================================");

                // Homeowner
                User homeowner = new User();
                homeowner.setName("Home Owner");
                homeowner.setEmail("user@smarthome.com");
                homeowner.setPassword(passwordEncoder.encode("password"));
                homeowner.setRole(User.Role.HOMEOWNER);
                userRepository.save(homeowner);

                // Devices for Homeowner
                List<Device> devices = List.of(
                        createDevice(homeowner, "Living Room AC", "AC", 1500.0, true),
                        createDevice(homeowner, "Smart Fridge", "FRIDGE", 200.0, true),
                        createDevice(homeowner, "Bedroom Light", "LIGHT", 10.0, false),
                        createDevice(homeowner, "Water Heater", "HEATER", 3000.0, true));
                deviceRepository.saveAll(devices);

                System.out.println("Data Seeding Completed: Created users and devices.");
            }
        };
    }

    private Device createDevice(User user, String name, String type, Double power, Boolean status) {
        Device device = new Device();
        device.setUser(user);
        device.setName(name);
        device.setType(type);
        device.setPowerRating(power);
        device.setStatus(status);
        return device;
    }
}

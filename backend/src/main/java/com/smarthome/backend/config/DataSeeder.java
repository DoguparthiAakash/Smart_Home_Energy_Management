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
import java.util.Optional;

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
            Files.createDirectories(Paths.get("uploads"));

            // 0. Inspect Existing Users (Debug)
            System.out.println("----- EXISTING USERS IN DB -----");
            userRepository.findAll()
                    .forEach(u -> System.out.println("User: " + u.getEmail() + " | Role: " + u.getRole()));
            System.out.println("--------------------------------");

            // 1. Ensure Admin Exists (Upsert)
            User admin = userRepository.findByEmail("muterornament").orElseGet(() -> {
                User newAdmin = new User();
                newAdmin.setRole(User.Role.ADMIN);
                return newAdmin;
            });
            admin.setName("Admin User");
            admin.setEmail("muterornament");
            admin.setPassword(passwordEncoder.encode("DPfamily@5"));
            if (admin.getTwoFactorSecret() == null) {
                admin.setTwoFactorSecret(totpService.generateSecret().getKey());
            }
            userRepository.save(admin);
            System.out.println("ADMIN ACCOUNT ENSURED: muterornament");

            // 2. Ensure Guest Exists (Upsert)
            User guest = userRepository.findByEmail("guest").orElseGet(() -> {
                User newGuest = new User();
                newGuest.setRole(User.Role.HOMEOWNER);
                return newGuest;
            });
            guest.setName("Guest User");
            guest.setEmail("guest");
            guest.setPassword(passwordEncoder.encode("1234@5"));
            userRepository.save(guest); // Guest still uses HOMEOWNER role for permissions
            System.out.println("GUEST ACCOUNT ENSURED: guest");

            // 4. Ensure Devices Exist (if none)
            if (deviceRepository.count() == 0) {
                List<Device> devices = List.of(
                        createDevice(guest, "Living Room AC", "AC", 1500.0, true, false),
                        createDevice(guest, "Smart Fridge", "FRIDGE", 200.0, true, true),
                        createDevice(guest, "Bedroom Light", "LIGHT", 10.0, false, false),
                        createDevice(guest, "Water Heater", "HEATER", 3000.0, true, false));
                deviceRepository.saveAll(devices);
                System.out.println("DEVICES CREATED");
            }

            System.out.println("Data Seeding Completed.");
        };
    }

    private Device createDevice(User user, String name, String type, Double power, Boolean status, Boolean isCritical) {
        Device device = new Device();
        device.setUser(user);
        device.setName(name);
        device.setType(type);
        device.setPowerRating(power);
        device.setStatus(status);
        device.setIsCritical(isCritical);
        return device;
    }
}

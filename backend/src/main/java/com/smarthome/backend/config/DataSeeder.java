package com.smarthome.backend.config;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.UserRepository;
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
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            Files.createDirectories(Paths.get("uploads"));

            System.out.println("----- EXISTING USERS IN DB (Audit) -----");
            userRepository.findAll()
                    .forEach(u -> System.out.println("User: " + u.getEmail() + " | Role: " + u.getRole()));
            System.out.println("----------------------------------------");

            // 1. Ensure Admin Exists (Upsert)
            upsertUser("muterornament", "Admin User", "DPfamily@5", User.Role.ADMIN);

            // 2. Ensure Home User Exists (Upsert) - Full Access
            upsertUser("homeuser", "Home User", "password", User.Role.HOMEOWNER);

            // 3. Ensure Guest User Exists (Upsert) - Read Only
            upsertUser("guest", "Guest User", "guest", User.Role.GUEST);

            // 4. Ensure Devices Exist (Associate with Home User)
            if (deviceRepository.count() == 0) {
                User homeUser = userRepository.findByEmail("homeuser").get();
                List<Device> devices = List.of(
                        createDevice(homeUser, "Living Room AC", "AC", 1500.0, true, Device.Priority.MEDIUM),
                        createDevice(homeUser, "Smart Fridge", "FRIDGE", 200.0, true, Device.Priority.HIGH),
                        createDevice(homeUser, "Bedroom Light", "LIGHT", 10.0, false, Device.Priority.LOW),
                        createDevice(homeUser, "Water Heater", "HEATER", 3000.0, true, Device.Priority.MEDIUM));
                deviceRepository.saveAll(devices);
                System.out.println("DEVICES CREATED");
            } else {
                // Ensure specific simulation device exists (User Request)
                User homeUser = userRepository.findByEmail("homeuser").get();
                boolean hasEv = deviceRepository.findByUserId(homeUser.getId()).stream()
                        .anyMatch(d -> d.getName().equals("EV Charger"));
                if (!hasEv) {
                    Device ev = createDevice(homeUser, "EV Charger", "EV", 7200.0, true, Device.Priority.LOW);
                    deviceRepository.save(ev);
                    System.out.println("SAMPLE DEVICE 'EV Charger' ADDED");
                }
            }
        };
    }

    private void upsertUser(String email, String name, String rawPassword, User.Role role) {
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            return newUser;
        });
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        // Default Max Wattage for everyone
        if (user.getMaxWattage() == null) {
            user.setMaxWattage(3000.0); // Example: 3000W limit
        }
        userRepository.save(user);
        System.out.println("UPSERTED USER: " + email + " (" + role + ")");
    }

    private Device createDevice(User user, String name, String type, Double power, Boolean status,
            Device.Priority priority) {
        Device device = new Device();
        device.setUser(user);
        device.setName(name);
        device.setType(type);
        device.setPowerRating(power);
        device.setStatus(status);
        device.setPriority(priority);
        return device;
    }
}

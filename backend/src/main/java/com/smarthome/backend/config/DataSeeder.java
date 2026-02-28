package com.smarthome.backend.config;

import com.smarthome.backend.model.Device;
import com.smarthome.backend.model.TechnicianVisit;
import com.smarthome.backend.model.UsageLog;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.DeviceRepository;
import com.smarthome.backend.repository.TechnicianVisitRepository;
import com.smarthome.backend.repository.UsageLogRepository;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Configuration
public class DataSeeder {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UsageLogRepository usageLogRepository;
    @Autowired
    private TechnicianVisitRepository visitRepository;

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            Files.createDirectories(Paths.get("uploads"));

            System.out.println("----- EXISTING USERS IN DB (Audit) -----");
            userRepository.findAll()
                    .forEach(u -> System.out.println("User: " + u.getEmail() + " | Role: " + u.getRole()));
            System.out.println("----------------------------------------");

            upsertUser("muterornament", "Head Admin", "password", User.Role.ADMIN);
            upsertUser("admin_user", "Admin User", "password", User.Role.ADMIN);
            upsertUser("john_homeowner", "John Homeowner", "password", User.Role.HOMEOWNER);
            upsertUser("guest", "Guest User", "guest", User.Role.GUEST);
            upsertUser("technician", "technician", "password", User.Role.TECHNICIAN);

            User homeUser = userRepository.findByEmail("john_homeowner").get();
            if (deviceRepository.count() == 0) {
                List<Device> devices = List.of(
                        createDevice(homeUser, "Living Room AC", "AC", 1500.0, true, Device.Priority.MEDIUM),
                        createDevice(homeUser, "Smart Fridge", "FRIDGE", 200.0, true, Device.Priority.HIGH),
                        createDevice(homeUser, "Bedroom Light", "LIGHT", 10.0, false, Device.Priority.LOW),
                        createDevice(homeUser, "Water Heater", "HEATER", 3000.0, true, Device.Priority.MEDIUM));
                deviceRepository.saveAll(devices);
                System.out.println("DEVICES CREATED");
            } else {
                boolean hasEv = deviceRepository.findByUserId(homeUser.getId()).stream()
                        .anyMatch(d -> d.getName().equals("EV Charger"));
                if (!hasEv) {
                    deviceRepository
                            .save(createDevice(homeUser, "EV Charger", "EV", 7200.0, false, Device.Priority.LOW));
                    System.out.println("SAMPLE DEVICE 'EV Charger' ADDED");
                }
            }

            seedHistoricalUsage(homeUser);
            seedTechnicianVisits();
        };
    }

    private void seedTechnicianVisits() {
        if (visitRepository.count() > 0) return;

        User tech = userRepository.findByEmail("technician").orElse(null);
        if (tech == null) return;

        Random rnd = new Random(55);
        LocalDate today = LocalDate.now();

        // 1 Active visit
        TechnicianVisit active = new TechnicianVisit();
        active.setTechnician(tech);
        active.setVisitDate(today);
        active.setStartTime(LocalDateTime.of(today, LocalTime.of(9, 30)));
        active.setStatus("ACTIVE");
        visitRepository.save(active);

        // 2 Completed visits
        for (int i = 1; i <= 2; i++) {
            LocalDate date = today.minusDays(i);
            TechnicianVisit complete = new TechnicianVisit();
            complete.setTechnician(tech);
            complete.setVisitDate(date);
            complete.setStartTime(LocalDateTime.of(date, LocalTime.of(10, 0)));
            complete.setEndTime(LocalDateTime.of(date, LocalTime.of(14, 30)));
            complete.setStatus("LOGGED_OUT");
            visitRepository.save(complete);
        }
        System.out.println("TECHNICIAN VISITS SEEDED");
    }

    private void seedHistoricalUsage(User homeUser) {
        LocalDate today = LocalDate.now();

        // Count how many of the past 7 days already have usage logs
        long daysWithData = 0;
        for (int i = 1; i <= 7; i++) {
            LocalDate day = today.minusDays(i);
            LocalDateTime s = LocalDateTime.of(day, LocalTime.MIN);
            LocalDateTime e = LocalDateTime.of(day, LocalTime.MAX);
            if (!usageLogRepository.findByUserIdAndTimestampBetween(homeUser.getId(), s, e).isEmpty())
                daysWithData++;
        }
        if (daysWithData >= 6) {
            System.out.println("Historical data present for " + daysWithData + "/7 days – skipping seed.");
            return;
        }

        System.out.println("Seeding missing historical usage days (" + daysWithData + "/7 populated)...");
        List<Device> devices = deviceRepository.findByUserId(homeUser.getId()).stream()
                .filter(d -> d.getPowerRating() != null && d.getPowerRating() > 0)
                .toList();
        if (devices.isEmpty())
            return;

        Random rnd = new Random(42);
        int intervalSec = 30;

        for (int dayOffset = 7; dayOffset >= 1; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            LocalDateTime s = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime e = LocalDateTime.of(date, LocalTime.MAX);

            // Skip days that already have logs
            if (!usageLogRepository.findByUserIdAndTimestampBetween(homeUser.getId(), s, e).isEmpty()) {
                System.out.println("Skipping " + date + " (already has data)");
                continue;
            }

            double totalKwh = 0;
            for (Device device : devices) {
                double hoursOn = typicalHours(device.getType(), rnd);
                long totalIntervals = (long) (hoursOn * 3600.0 / intervalSec);
                LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.of(7, 0));

                for (long i = 0; i < totalIntervals; i++) {
                    LocalDateTime ts = dayStart.plusSeconds(i * intervalSec);
                    LocalDateTime tsEnd = ts.plusSeconds(intervalSec);

                    double jitter = 1.0 + (rnd.nextDouble() * 0.30) - 0.15; // ±15%
                    double energyKwh = (device.getPowerRating() * jitter * intervalSec) / 3_600_000.0;

                    UsageLog logEntry = new UsageLog();
                    logEntry.setDevice(device);
                    logEntry.setStartTime(ts);
                    logEntry.setEndTime(tsEnd);
                    logEntry.setTimestamp(tsEnd);
                    logEntry.setEnergyKwh(energyKwh);
                    usageLogRepository.save(logEntry);
                    totalKwh += energyKwh;
                }
            }
            System.out.printf("Seeded %s – %.3f kWh%n", date, totalKwh);
        }
        System.out.println("Historical seed complete.");
    }

    /** Realistic typical daily ON-hours by device type */
    private double typicalHours(String type, Random rnd) {
        return switch (type.toUpperCase()) {
            case "AC" -> 6 + rnd.nextDouble() * 2; // 6–8 h
            case "FRIDGE" -> 20 + rnd.nextDouble() * 3; // 20–23 h
            case "LIGHT" -> 4 + rnd.nextDouble() * 3; // 4–7 h
            case "HEATER" -> 2 + rnd.nextDouble() * 2; // 2–4 h
            case "FAN" -> 5 + rnd.nextDouble() * 4; // 5–9 h
            case "EV" -> 2 + rnd.nextDouble() * 2; // 2–4 h
            default -> 3 + rnd.nextDouble() * 2; // 3–5 h
        };
    }

    private void upsertUser(String email, String name, String rawPassword, User.Role role) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setEmail(email);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        if (user.getMaxWattage() == null)
            user.setMaxWattage(5000.0);
        userRepository.save(user);
        System.out.println("UPSERTED USER: " + email + " (" + role + ")");
    }

    private Device createDevice(User user, String name, String type, Double power,
            Boolean status, Device.Priority priority) {
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

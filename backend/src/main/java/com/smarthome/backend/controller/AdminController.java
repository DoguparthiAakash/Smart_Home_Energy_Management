package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.model.TechnicianVisit;
import com.smarthome.backend.repository.TechnicianVisitRepository;
import com.smarthome.backend.model.SystemEvent;
import com.smarthome.backend.repository.SystemEventRepository;
import com.smarthome.backend.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/pending-technicians")
    public List<User> getPendingTechnicians() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.TECHNICIAN && !u.isApproved())
                .collect(Collectors.toList());
    }

    @PostMapping("/approve-technician/{id}")
    public ResponseEntity<?> approveTechnician(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            user.setApproved(true);
            userRepository.save(user);
            return ResponseEntity.ok("Technician approved");
        }).orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/technicians")
    public List<java.util.Map<String, Object>> getApprovedTechnicians() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.TECHNICIAN && u.isApproved())
                .map(u -> {
                    java.util.Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("email", u.getEmail());

                    long total = visitRepository.countByTechnicianId(u.getId());
                    long completed = visitRepository.countByTechnicianIdAndStatus(u.getId(), "COMPLETED");
                    double rate = total == 0 ? 0 : (completed * 100.0 / total);

                    map.put("totalVisits", total);
                    map.put("completionRate", Math.round(rate));
                    map.put("rating", total == 0 ? 0.0 : 4.5 + (new java.util.Random().nextDouble() * 0.5));
                    return map;
                })
                .collect(Collectors.toList());
    }

    @PostMapping("/technicians")
    public ResponseEntity<?> createTechnician(@RequestBody User tech) {
        if (userRepository.findByEmail(tech.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        tech.setRole(User.Role.TECHNICIAN);
        tech.setApproved(true);
        tech.setPassword(passwordEncoder.encode(tech.getPassword()));
        userRepository.save(tech);
        return ResponseEntity.ok(tech);
    }

    @Autowired
    private TechnicianVisitRepository visitRepository;

    @GetMapping("/technician-visits")
    public List<TechnicianVisit> getTechnicianVisits(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        if (start != null && end != null) {
            return visitRepository.findByVisitDateBetweenOrderByVisitDateDescStartTimeDesc(start, end);
        }
        return visitRepository.findTop10ByOrderByStartTimeDesc();
    }

    @GetMapping("/technician-visits/dates")
    public List<LocalDate> getVisitDates() {
        return visitRepository.findDistinctVisitDateByOrderByVisitDateDesc();
    }

    @GetMapping("/download-id/{userId}")
    public ResponseEntity<Resource> downloadId(@PathVariable Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        try {
            Path filePath = Paths.get("uploads").resolve(user.getIdDocumentPath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @Autowired
    private com.smarthome.backend.service.DeviceSimulatorService simulatorService;

    @GetMapping("/simulator/status")
    public java.util.Map<String, Object> getSimulatorStatus() {
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("enabled", simulatorService.isEnabled());
        return status;
    }

    @PostMapping("/simulator/toggle")
    public java.util.Map<String, Object> toggleSimulator(@RequestBody java.util.Map<String, Boolean> request) {
        boolean enabled = request.getOrDefault("enabled", false);
        simulatorService.setEnabled(enabled);
        java.util.Map<String, Object> status = new java.util.HashMap<>();
        status.put("enabled", simulatorService.isEnabled());
        return status;
    }

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SystemEventRepository eventRepository;

    @GetMapping("/stats")
    public java.util.Map<String, Object> getAdminStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("activeSystems", deviceRepository.countByStatus(true));
        stats.put("systemHealth", 99.9);

        // Energy Throttled calculation
        long throttledEvents = eventRepository.countByType("ENERGY_MANAGEMENT");
        stats.put("energyThrottled", throttledEvents * 1.5 + (new java.util.Random().nextDouble() * 0.5));

        // Security Overview Stats
        long failedLogins = eventRepository.countByType("SECURITY");
        stats.put("failedLogins", failedLogins);
        stats.put("activeSessions", userRepository.count());

        // Global System Load (Feature from Home User)
        double totalLoad = deviceRepository.findByStatusTrue().stream()
                .mapToDouble(com.smarthome.backend.model.Device::getPowerRating)
                .sum();
        double maxCapacity = userRepository.findAll().stream()
                .mapToDouble(u -> u.getMaxWattage() != null ? u.getMaxWattage() : 5000.0)
                .sum();

        stats.put("currentLoad", totalLoad);
        stats.put("maxLoad", maxCapacity);

        return stats;
    }

    @GetMapping("/health")
    public java.util.Map<String, Object> getSystemHealth() {
        java.util.Map<String, Object> health = new java.util.HashMap<>();
        health.put("database", "Connected");
        health.put("apiGateway", "Stable");
        health.put("mqttBroker", (new java.util.Random().nextInt(15) + 5) + "ms");

        // Slightly more dynamic loads
        java.util.Random rand = new java.util.Random();
        health.put("dbLoad", 90 + rand.nextInt(10));
        health.put("apiLoad", 80 + rand.nextInt(15));
        health.put("mqttLoad", 85 + rand.nextInt(10));
        return health;
    }

    @GetMapping("/events")
    public List<SystemEvent> getSystemEvents() {
        return eventRepository.findTop20ByOrderByTimestampDesc();
    }

    @PostMapping("/events")
    public SystemEvent createEvent(@RequestBody SystemEvent event) {
        event.setTimestamp(java.time.LocalDateTime.now());
        if (event.getSeverity() == null)
            event.setSeverity(SystemEvent.Severity.INFO);
        return eventRepository.save(event);
    }
}

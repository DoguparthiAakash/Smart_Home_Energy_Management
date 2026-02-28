package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.model.TechnicianVisit;
import com.smarthome.backend.repository.TechnicianVisitRepository;
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
    public List<User> getApprovedTechnicians() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == User.Role.TECHNICIAN && u.isApproved())
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
}

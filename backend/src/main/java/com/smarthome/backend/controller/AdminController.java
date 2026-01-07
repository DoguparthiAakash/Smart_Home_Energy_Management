package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
}

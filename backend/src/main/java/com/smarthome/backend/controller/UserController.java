package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Object> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (payload.containsKey("maxWattage")) {
            try {
                Double maxW = Double.valueOf(payload.get("maxWattage").toString());
                user.setMaxWattage(maxW);
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Settings updated", "maxWattage", maxW));
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format"));
            }
        }

        return ResponseEntity.badRequest().body(Map.of("error", "No valid settings provided"));
    }
}

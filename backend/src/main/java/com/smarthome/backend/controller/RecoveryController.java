package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.service.PasswordRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recovery")
public class RecoveryController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordRecoveryService recoveryService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Save Recovery Options (Authenticated)
    @PostMapping("/options")
    public ResponseEntity<?> saveOptions(@RequestBody Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (payload.containsKey("mobile")) {
            user.setMobileNumber(payload.get("mobile"));
        }
        if (payload.containsKey("recoveryEmail")) {
            user.setRecoveryEmail(payload.get("recoveryEmail"));
        }

        userRepository.save(user);
        return ResponseEntity.ok().body(Map.of("message", "Recovery options saved successfully"));
    }
    
    // Get Recovery Options (Authenticated)
    @GetMapping("/options")
    public ResponseEntity<?> getOptions(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        
        Map<String, String> response = new HashMap<>();
        response.put("mobile", user.getMobileNumber());
        response.put("recoveryEmail", user.getRecoveryEmail());
        
        return ResponseEntity.ok(response);
    }

    // Initiate Recovery (Public)
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateRecovery(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        Optional<User> userOpt = userRepository.findByEmail(username); // Username is email in this system
        
        // If not found by email, try to find by 'name' if uniqueness allowed?
        // System uses email as username for login mostly. Let's assume input is email or we search by name.
        if (userOpt.isEmpty()) {
             // Try searching by name? 
             // Ideally we should warn user, but for security we might say "If account exists..."
             // For this demo, let's be explicit.
             return ResponseEntity.badRequest().body("User not found");
        }
        
        User user = userOpt.get();
        String method = payload.get("method"); // "mobile" or "email"
        String target = null;
        
        if ("mobile".equalsIgnoreCase(method)) {
            target = user.getMobileNumber();
            if (target == null || target.isEmpty()) {
                return ResponseEntity.badRequest().body("No mobile number registered for this account.");
            }
        } else if ("email".equalsIgnoreCase(method)) {
            target = user.getRecoveryEmail();
            if (target == null || target.isEmpty()) {
                 // Fallback to primary email? User requested "Recovery Email explicitly".
                 // Let's allow fallback to primary if they choose email recovery? 
                 // Plan said "Recovery Email". Let's stick to that.
                 return ResponseEntity.badRequest().body("No recovery email registered. Contact Admin.");
            }
        } else {
            return ResponseEntity.badRequest().body("Invalid recovery method.");
        }
        
        recoveryService.sendOtp(username, method); // Log OTP for the USER (keyed by username/primary email for verification)
        return ResponseEntity.ok().body(Map.of("message", "OTP sent to your " + method));
    }

    // Reset Password (Public) - Verifies OTP and Resets
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String otp = payload.get("otp");
        String newPassword = payload.get("newPassword");
        
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isEmpty()) {
             return ResponseEntity.badRequest().body("User not found");
        }
        
        // Verify OTP (Consumes it)
        boolean isValid = recoveryService.verifyOtp(username, otp);
        if (!isValid) {
            return ResponseEntity.badRequest().body("Invalid or Expired OTP");
        }
        
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok().body(Map.of("message", "Password reset successfully. You may login now."));
    }
}

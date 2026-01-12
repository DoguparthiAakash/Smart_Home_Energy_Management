package com.smarthome.backend.controller;

import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
public class TwoFactorController {

    @Autowired
    private UserRepository userRepository;

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    @PostMapping("/generate")
    public ResponseEntity<?> generateSecret(Authentication authentication) {
        String email = authentication.getName();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();

        // Return secret and detailed OTPAuth URL for QR Code
        // Hostname "SmartHome" and Issuer "EnergyManager" will appear in the app
        String otpAuthUrl = "otpauth://totp/SmartHome:" + email + "?secret=" + secret + "&issuer=EnergyManager";

        Map<String, String> response = new HashMap<>();
        response.put("secret", secret);
        response.put("otpAuthUrl", otpAuthUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable2FA(@RequestBody Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        String codeStr = payload.get("code");
        String secret = payload.get("secret");

        if (codeStr == null || secret == null || codeStr.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing code or secret");
        }

        int code;
        try {
            code = Integer.parseInt(codeStr.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Invalid code format");
        }

        if (gAuth.authorize(secret, code)) {
            User user = userRepository.findByEmail(email).orElseThrow();
            user.setTwoFactorSecret(secret);
            userRepository.save(user);
            return ResponseEntity.ok("2FA Enabled");
        }

        return ResponseEntity.badRequest().body("Invalid Code");
    }

    @PostMapping("/verify-login")
    public ResponseEntity<?> verifyLogin(@RequestBody Map<String, String> payload, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        if (user.getTwoFactorSecret() == null) {
            return ResponseEntity.ok("2FA Not Enabled");
        }

        String codeStr = payload.get("code");
        if (codeStr == null || codeStr.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing code");
        }

        int code;
        try {
            code = Integer.parseInt(codeStr.trim());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(401).body("Invalid code format");
        }

        if (gAuth.authorize(user.getTwoFactorSecret(), code)) {
            return ResponseEntity.ok("Verified");
        }

        return ResponseEntity.status(401).body("Invalid Code");
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        return ResponseEntity.ok("2FA Disabled");
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Map<String, Boolean> response = new HashMap<>();
        response.put("enabled", user.getTwoFactorSecret() != null && !user.getTwoFactorSecret().isEmpty());
        return ResponseEntity.ok(response);
    }
}

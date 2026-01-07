package com.smarthome.backend.controller;

import com.smarthome.backend.dto.AuthRequest;
import com.smarthome.backend.dto.AuthResponse;
import com.smarthome.backend.model.User;
import com.smarthome.backend.repository.UserRepository;
import com.smarthome.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.smarthome.backend.service.TotpService totpService;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequest authRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));
        } catch (Exception e) {
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        User user = userRepository.findByEmail(authRequest.getEmail()).get();

        // 0. Validate Role Selection
        if (!user.getRole().toString().equalsIgnoreCase(authRequest.getRole())) {
            return ResponseEntity.status(403).body("Invalid Role selected for this account.");
        }

        // 1. Check Technician Approval
        if (user.getRole() == User.Role.TECHNICIAN && !user.isApproved()) {
            return ResponseEntity.status(403).body("Account pending approval.");
        }

        // 2. Check Admin 2FA
        if (user.getRole() == User.Role.ADMIN) {
            // For Admin, we don't issue token yet. We tell frontend to ask for OTP.
            // But for simplicity in this REST flow, let's assume the frontend sends code IF
            // prompt.
            // However, standard flow: Return "REQUIRE_2FA" -> Frontend shows input -> User
            // calls /verify-2fa
            return ResponseEntity.accepted().body("REQUIRE_2FA");
        }

        final String jwt = jwtUtil.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().toString()));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody java.util.Map<String, String> payload) {
        String email = payload.get("email");
        int code = Integer.parseInt(payload.get("code"));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

        if (totpService.verifyCode(user.getTwoFactorSecret(), code)) {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            final String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getRole().toString()));
        }
        return ResponseEntity.status(401).body("Invalid 2FA Code");
    }

    @PostMapping(value = "/register", consumes = { "multipart/form-data" })
    public ResponseEntity<?> registerUser(@RequestPart("user") User user,
            @RequestPart(value = "file", required = false) org.springframework.web.multipart.MultipartFile file) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRole() == User.Role.TECHNICIAN) {
            user.setApproved(false);
            if (file != null && !file.isEmpty()) {
                // Save file logic (Simplified: save to local disk)
                String fileName = "tech_id_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                try {
                    java.nio.file.Files.copy(file.getInputStream(), java.nio.file.Paths.get("uploads", fileName));
                    user.setIdDocumentPath(fileName);
                } catch (java.io.IOException e) {
                    return ResponseEntity.internalServerError().body("Failed to upload ID");
                }
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}

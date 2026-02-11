package com.smarthome.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordRecoveryService {

    // Storage: Target (Email/Mobile) -> OtpData
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String target) {
        String otp = String.format("%06d", random.nextInt(999999));
        otpStorage.put(target, new OtpData(otp, LocalDateTime.now().plusMinutes(5))); // 5 mins validity
        return otp;
    }

    public void sendOtp(String target, String type) {
        String otp = generateOtp(target);
        // Simulation: Print to Console
        System.out.println("==================================================");
        System.out.println(" PASSWORD RECOVERY OTP (" + type.toUpperCase() + ")");
        System.out.println(" Target: " + target);
        System.out.println(" OTP:    " + otp);
        System.out.println("==================================================");

        // Simulation: Log to File for verification
        try {
            java.nio.file.Files.writeString(
                    java.nio.file.Paths.get("/home/aakash/Downloads/Smart_Home_Energy_Management/recovery_otp.txt"),
                    "Target: " + target + "\nOTP: " + otp + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifyOtp(String target, String otp) {
        OtpData data = otpStorage.get(target);
        if (data == null) {
            return false;
        }

        if (data.expiry.isBefore(LocalDateTime.now())) {
            otpStorage.remove(target);
            return false;
        }

        if (data.otp.equals(otp)) {
            otpStorage.remove(target); // Consume OTP
            return true;
        }

        return false;
    }

    private static class OtpData {
        String otp;
        LocalDateTime expiry;

        public OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}

package com.smarthome.backend.service;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class TotpService {

    private final GoogleAuthenticator gAuth;

    public TotpService() {
        this.gAuth = new GoogleAuthenticator();
    }

    public GoogleAuthenticatorKey generateSecret() {
        return gAuth.createCredentials();
    }

    public String getQrCodeUrl(GoogleAuthenticatorKey key, String user) {
        return GoogleAuthenticatorQRGenerator.getOtpAuthURL("SmartHome", user, key);
    }

    public String getOtpAuthUrl(String secret, String user) {
        // Manual construction if needed, but GoogleAuthenticatorQRGenerator is easier
        return "otpauth://totp/SmartHome:" + user + "?secret=" + secret + "&issuer=SmartHome";
    }

    public boolean verifyCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}

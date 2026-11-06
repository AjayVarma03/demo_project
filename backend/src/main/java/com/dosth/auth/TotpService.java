package com.dosth.auth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import org.springframework.stereotype.Service;

@Service
public class TotpService {
    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public record TotpSetup(String secret, String qrCodeUrl) {}

    public TotpSetup generateSecret(String phone) {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String qrUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL("DosthApp", phone, key);
        return new TotpSetup(key.getKey(), qrUrl);
    }

    public boolean verifyCode(String secret, int verificationCode) {
        return gAuth.authorize(secret, verificationCode);
    }
}

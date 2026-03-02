package vault.service;

import java.security.SecureRandom;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import vault.config.AppConfig;

/**
 * Sends OTP via Twilio SMS. Requires twilio.accountSid, twilio.authToken, twilio.fromNumber in application.properties.
 */
public final class SmsOtpService {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private static boolean twilioInitialized;

    public SmsOtpService(AppConfig config) {
        this.accountSid = config.twilioAccountSid();
        this.authToken = config.twilioAuthToken();
        this.fromNumber = config.twilioFromNumber();
    }

    /**
     * Generates a 6-digit OTP, sends it via SMS to the given phone number (E.164 format, e.g. +919876543210),
     * and returns the same OTP for verification.
     *
     * @param toPhone E.164 format, e.g. +919876543210
     * @return the OTP that was sent, or null if SMS failed
     */
    public String sendOtp(String toPhone) {
        if (accountSid.isEmpty() || authToken.isEmpty() || fromNumber.isEmpty()) {
            System.out.println("SMS not configured. Set twilio.accountSid, twilio.authToken, twilio.fromNumber in application.properties");
            return null;
        }
        String otp = generateOtp();
        try {
            synchronized (SmsOtpService.class) {
                if (!twilioInitialized) {
                    Twilio.init(accountSid, authToken);
                    twilioInitialized = true;
                }
            }
            String body = "Your Password Vault verification code is: " + otp + ". Do not share.";
            Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(fromNumber),
                    body)
                    .create();
            return otp;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            System.out.println("Failed to send SMS: " + msg);
            if (msg.contains("Invalid") && msg.contains("Phone")) {
                System.out.println("Tip: Use E.164 format. India: +919876543210. US/Canada: +11234567890. Trial accounts: verify the number in Twilio Console.");
            }
            return null;
        }
    }

    private static String generateOtp() {
        SecureRandom random = new SecureRandom();
        int value = random.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}

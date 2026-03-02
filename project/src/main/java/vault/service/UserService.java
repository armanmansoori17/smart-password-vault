package vault.service;

import java.sql.SQLException;
import java.util.Optional;
import java.util.Scanner;

import vault.config.AppConfig;
import vault.dao.UserDao;
import vault.model.User;
import vault.util.CryptoUtil;
import vault.util.PasswordValidator;

public final class UserService {

    private static final int MAX_ATTEMPTS = 3;
    private static int attempts = 0;

    private final UserDao userDao = new UserDao();
    private final SmsOtpService smsOtpService = new SmsOtpService(AppConfig.load());

    public int register(Scanner sc) {
        try {
            System.out.print("Enter username: ");
            String username = sc.nextLine().trim();
            if (username.isBlank()) {
                System.out.println("Username cannot be empty");
                return -1;
            }

            System.out.print("Enter phone number (E.164, e.g. +919876543210 or +11234567890): ");
            String phone = sc.nextLine().trim();
            if (phone.isBlank()) {
                System.out.println("Phone number cannot be empty");
                return -1;
            }
            String toPhone = normalizePhone(phone);
            if (toPhone == null) {
                System.out.println("Invalid format. Use E.164: + followed by 10-15 digits (e.g. India +919876543210, US +11234567890)");
                return -1;
            }

            String password;
            while (true) {
                System.out.println("Requirements: " + PasswordValidator.getRequirements());
                System.out.print("Enter master password: ");
                password = sc.nextLine();
                String err = PasswordValidator.validate(password);
                if (err == null) break;
                System.out.println(err);
            }

            String encrypted = CryptoUtil.encrypt(password);
            if (encrypted == null) {
                System.out.println("Encryption failed");
                return -1;
            }

            String otp = smsOtpService.sendOtp(toPhone);
            if (otp == null) {
                System.out.println("Could not send OTP SMS. Check Twilio config and try again.");
                return -1;
            }
            System.out.println("OTP sent to your phone. Enter the code below.");

            int maxOtpAttempts = 3;
            boolean verified = false;
            for (int i = 1; i <= maxOtpAttempts; i++) {
                System.out.print("Enter OTP: ");
                String input = sc.nextLine().trim();
                if (otp.equals(input)) {
                    verified = true;
                    break;
                }
                int left = maxOtpAttempts - i;
                if (left > 0) {
                    System.out.println("Invalid OTP (" + left + " attempt(s) left)");
                }
            }

            if (!verified) {
                System.out.println("OTP verification failed. Registration cancelled.");
                return -1;
            }

            int id = userDao.insert(username, encrypted, toPhone, true);
            System.out.println("Registration successful");
            return id;
        } catch (SQLException e) {
            if (e.getMessage() != null && (e.getMessage().contains("Duplicate") || e.getMessage().contains("UNIQUE"))) {
                System.out.println("User exists or error: Username already taken");
            } else {
                System.out.println("User exists or error: " + e.getMessage());
            }
            return -1;
        }
    }

    public int login(Scanner sc) {
        try {
            System.out.print("Username: ");
            String username = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine();

            Optional<User> userOpt = userDao.findByUsername(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (!user.phoneVerified()) {
                    System.out.println("Phone number not verified. Please register again.");
                    return -1;
                }
                String stored = user.encryptedMasterPassword();
                String decrypted = CryptoUtil.decrypt(stored);

                if (password != null && password.equals(decrypted)) {
                    attempts = 0;
                    System.out.println("Login successful");
                    return user.id();
                }
            }

            attempts++;
            if (attempts >= MAX_ATTEMPTS) {
                System.out.println("Account locked (3 failed attempts)");
                return -1;
            }
            System.out.println("Invalid credentials (" + (MAX_ATTEMPTS - attempts) + " attempts left)");
            return -1;
        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
            return -1;
        }
    }

    private static String normalizePhone(String input) {
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) return null;
        String withPlus = "+" + digits;
        if (digits.length() < 10 || digits.length() > 15) return null;
        return withPlus;
    }
}

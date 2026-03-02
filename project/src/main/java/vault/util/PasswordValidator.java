package vault.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates password against regex rules. Returns list of missing requirements.
 */
public final class PasswordValidator {

    private static final int MIN_LENGTH = 8;

    /**
     * Validates password. Returns null if valid, otherwise a message listing what's missing.
     */
    public static String validate(String password) {
        if (password == null || password.isEmpty()) {
            return "Password cannot be empty";
        }
        List<String> missing = new ArrayList<>();
        if (password.length() < MIN_LENGTH) {
            missing.add("at least " + MIN_LENGTH + " characters");
        }
        if (!password.matches(".*[A-Z].*")) {
            missing.add("one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            missing.add("one lowercase letter");
        }
        if (!password.matches(".*[0-9].*")) {
            missing.add("one digit");
        }
        if (!password.matches(".*[@#$%!&*].*")) {
            missing.add("one special character (@#$%!&*)");
        }
        if (missing.isEmpty()) return null;
        return "Missing: " + String.join(", ", missing) + ". Retype password.";
    }

    public static String getRequirements() {
        return "Password must have: min " + MIN_LENGTH + " chars, uppercase, lowercase, digit, special (@#$%!&*)";
    }

    private PasswordValidator() {}
}

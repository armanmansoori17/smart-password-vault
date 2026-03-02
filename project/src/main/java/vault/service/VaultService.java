package vault.service;

import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import vault.dao.VaultEntryDao;
import vault.model.VaultEntry;
import vault.model.VaultEntryExport;
import vault.util.CryptoUtil;
import vault.util.PasswordValidator;

public final class VaultService {

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%!";

    private final VaultEntryDao vaultEntryDao = new VaultEntryDao();

    public void addEntry(int userId, Scanner sc) {
        try {
            System.out.print("Site/App name: ");
            String site = sc.nextLine().trim();
            System.out.print("Login username/email: ");
            String loginUser = sc.nextLine().trim();
            System.out.print("Password: ");
            String password = sc.nextLine();

            if (site.isBlank() || loginUser.isBlank() || password == null || password.isEmpty()) {
                System.out.println("Site, username, and password are required");
                return;
            }

            if (vaultEntryDao.existsDuplicate(userId, site, loginUser)) {
                System.out.println("Credential already exists");
                return;
            }

            String encrypted = CryptoUtil.encrypt(password);
            if (encrypted == null) {
                System.out.println("Encryption failed");
                return;
            }

            String strength = checkStrength(password);
            System.out.println("Password Strength: " + strength);

            vaultEntryDao.insert(userId, site, loginUser, encrypted, null, null);
            System.out.println("Credential saved");
        } catch (SQLException e) {
            System.out.println("Add failed: " + e.getMessage());
        }
    }

    public void viewEntries(int userId) {
        try {
            List<VaultEntry> entries = vaultEntryDao.findAllByUserId(userId);
            System.out.println("\n==== SAVED CREDENTIALS ====");
            if (entries.isEmpty()) {
                System.out.println("(No entries)");
                return;
            }
            for (VaultEntry e : entries) {
                String decrypted = CryptoUtil.decrypt(e.encryptedPassword());
                System.out.println("ID: " + e.id());
                System.out.println("Site: " + e.siteName());
                System.out.println("Username: " + e.loginUsername());
                System.out.println("Password: " + decrypted);
                System.out.println("----------------------");
                try {
                    vaultEntryDao.updateLastAccessed(e.id());
                } catch (SQLException ignored) { }
            }
        } catch (SQLException e) {
            System.out.println("View error: " + e.getMessage());
        }
    }

    public void searchEntry(int userId, Scanner sc) {
        try {
            System.out.print("Enter site name to search: ");
            String site = sc.nextLine().trim();
            List<VaultEntry> entries = vaultEntryDao.searchBySite(userId, site);
            if (entries.isEmpty()) {
                System.out.println("No matches found");
                return;
            }
            for (VaultEntry e : entries) {
                String decrypted = CryptoUtil.decrypt(e.encryptedPassword());
                System.out.println("\nID: " + e.id());
                System.out.println("Site: " + e.siteName());
                System.out.println("Username: " + e.loginUsername());
                System.out.println("Password: " + decrypted);
            }
        } catch (SQLException e) {
            System.out.println("Search error: " + e.getMessage());
        }
    }

    public void updateEntry(int userId, Scanner sc) {
        try {
            System.out.print("Enter entry ID to update: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("New username: ");
            String username = sc.nextLine().trim();

            String password;
            while (true) {
                System.out.println("Requirements: " + PasswordValidator.getRequirements());
                System.out.print("New password: ");
                password = sc.nextLine();
                String err = PasswordValidator.validate(password);
                if (err == null) break;
                System.out.println(err);
            }

            if (username.isBlank()) {
                System.out.println("Username is required");
                return;
            }

            String encrypted = CryptoUtil.encrypt(password);
            if (encrypted == null) {
                System.out.println("Encryption failed");
                return;
            }

            int rows = vaultEntryDao.update(id, userId, username, encrypted);
            if (rows > 0) {
                System.out.println("Updated successfully");
            } else {
                System.out.println("Entry not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        } catch (SQLException e) {
            System.out.println("Update error: " + e.getMessage());
        }
    }

    public void deleteEntry(int userId, Scanner sc) {
        try {
            System.out.print("Enter entry ID to delete: ");
            int id = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Confirm delete? (Y/N): ");
            String confirm = sc.nextLine().trim();
            if (!"Y".equalsIgnoreCase(confirm)) {
                System.out.println("Cancelled");
                return;
            }
            int rows = vaultEntryDao.delete(id, userId);
            if (rows > 0) {
                System.out.println("Deleted successfully");
            } else {
                System.out.println("Entry not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID");
        } catch (SQLException e) {
            System.out.println("Delete error: " + e.getMessage());
        }
    }

    public static String generatePassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder pass = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            pass.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return pass.toString();
    }

    public static String checkStrength(String password) {
        if (password == null || password.isEmpty()) return "Weak";
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[@#$%!].*")) score++;
        return switch (score) {
            case 4 -> "Strong";
            case 3 -> "Medium";
            default -> "Weak";
        };
    }

    public void exportVault(int userId) {
        try {
            List<VaultEntryExport> entries = vaultEntryDao.findAllForExport(userId);
            try (FileWriter writer = new FileWriter("vault_backup.txt")) {
                for (VaultEntryExport e : entries) {
                    writer.write(e.siteName() + " | " + e.loginUsername() + " | " + e.encryptedPassword() + "\n");
                }
            }
            System.out.println("Vault exported to vault_backup.txt");
        } catch (SQLException | IOException e) {
            System.out.println("Export error: " + e.getMessage());
        }
    }
}

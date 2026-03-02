package vault;

import java.util.Scanner;

import vault.service.UserService;
import vault.service.VaultService;

public final class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        UserService userService = new UserService();
        VaultService vaultService = new VaultService();
        int userId = -1;

        while (true) {
            if (userId == -1) {
                System.out.println("\n==== PASSWORD VAULT ====");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("3. Exit");
                System.out.print("Choose: ");

                String choice = sc.nextLine();
                switch (choice != null ? choice.trim() : "") {
                    case "1" -> userId = userService.login(sc);
                    case "2" -> userId = userService.register(sc);
                    case "3" -> {
                        System.out.println("Bye.");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice");
                }
            } else {
                System.out.println("\n==== VAULT MENU ====");
                System.out.println("1. Add Credential");
                System.out.println("2. View All");
                System.out.println("3. Search");
                System.out.println("4. Update Credential");
                System.out.println("5. Delete Credential");
                System.out.println("6. Generate Password");
                System.out.println("7. Export Vault");
                System.out.println("8. Logout");
                System.out.print("Choose: ");

                String choice = sc.nextLine();
                switch (choice != null ? choice.trim() : "") {
                    case "1" -> vaultService.addEntry(userId, sc);
                    case "2" -> vaultService.viewEntries(userId);
                    case "3" -> vaultService.searchEntry(userId, sc);
                    case "4" -> vaultService.updateEntry(userId, sc);
                    case "5" -> vaultService.deleteEntry(userId, sc);
                    case "6" -> {
                        String gen = VaultService.generatePassword();
                        System.out.println("Generated: " + gen);
                        System.out.println("Strength: " + VaultService.checkStrength(gen));
                    }
                    case "7" -> vaultService.exportVault(userId);
                    case "8" -> {
                        userId = -1;
                        System.out.println("Logged out.");
                    }
                    default -> System.out.println("Invalid choice");
                }
            }
        }
    }
}

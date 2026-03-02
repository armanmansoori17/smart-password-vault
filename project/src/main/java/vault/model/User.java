package vault.model;

/**
 * User entity from users table.
 */
public record User(int id, String username, String encryptedMasterPassword,
                   String phone, boolean phoneVerified) {
}

package vault.model;

import java.sql.Timestamp;

/**
 * Vault entry entity from vault_entries table.
 * Password is stored encrypted; decrypt via CryptoUtil in service layer.
 */
public record VaultEntry(
    int id,
    int userId,
    String siteName,
    String loginUsername,
    String encryptedPassword,
    String category,
    String notes,
    Timestamp createdAt,
    Timestamp lastAccessed
) {
}

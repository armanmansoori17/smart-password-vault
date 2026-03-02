package vault.model;

/**
 * Minimal vault entry for export (site, loginUser, encryptedPass).
 */
public record VaultEntryExport(String siteName, String loginUsername, String encryptedPassword) {
}

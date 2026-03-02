package vault.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES encryption utility for Password Vault.
 * Uses 16-byte key for project/interview level. Passwords are never stored in plain text.
 */
public final class CryptoUtil {

    private static final String KEY = "MySuperSecretKey"; // 16 chars for AES
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

    public static String encrypt(String data) {
        if (data == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public static String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isBlank()) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private CryptoUtil() {}
}

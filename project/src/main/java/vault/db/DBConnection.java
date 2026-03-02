
package vault.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import vault.config.AppConfig;

/**
 * JDBC connection utility for Password Vault.
 * Loads credentials from application.properties.
 */
public final class DBConnection {

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            AppConfig config = AppConfig.load();
            Properties props = new Properties();
            props.put("user", config.dbUser());
            props.put("password", config.dbPassword());
            return DriverManager.getConnection(config.dbUrl(), props);
        } catch (Exception e) {
            System.out.println("DB Connection Failed: " + e.getMessage());
            return null;
        }
    }

    private DBConnection() {}
}

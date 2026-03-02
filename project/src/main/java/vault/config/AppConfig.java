package vault.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AppConfig {
  private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/password_vault?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
  private static final String DEFAULT_USER = "root";
  private static final String DEFAULT_PASSWORD = "Asustufgamingf15";

  private final Properties props;

  private AppConfig(Properties props) {
    this.props = props;
  }

  public static AppConfig load() {
    Properties props = new Properties();
    try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (in != null) {
        props.load(in);
      }
    } catch (IOException ignored) {
    }
    return new AppConfig(props);
  }

  public String dbUrl() {
    String v = props.getProperty("db.url");
    return (v != null && !v.isBlank()) ? v.trim() : DEFAULT_URL;
  }

  public String dbUser() {
    String v = props.getProperty("db.user");
    return (v != null && !v.isBlank()) ? v.trim() : DEFAULT_USER;
  }

  public String dbPassword() {
    String v = props.getProperty("db.password");
    return (v != null && !v.isBlank()) ? v.trim() : DEFAULT_PASSWORD;
  }

  public String twilioAccountSid() {
    return props.getProperty("twilio.accountSid", "").trim();
  }

  public String twilioAuthToken() {
    return props.getProperty("twilio.authToken", "").trim();
  }

  public String twilioFromNumber() {
    return props.getProperty("twilio.fromNumber", "").trim();
  }

  public boolean isTwilioConfigured() {
    return !twilioAccountSid().isEmpty() && !twilioAuthToken().isEmpty() && !twilioFromNumber().isEmpty();
  }
}

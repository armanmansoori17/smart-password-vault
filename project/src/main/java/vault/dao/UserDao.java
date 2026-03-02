package vault.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import vault.db.DBConnection;
import vault.model.User;

public final class UserDao {

    public int insert(String username, String encryptedPassword, String phone, boolean phoneVerified) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(username, master_password, phone, phone_verified) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, encryptedPassword);
                ps.setString(3, phone);
                ps.setBoolean(4, phoneVerified);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                    throw new SQLException("No generated key returned");
                }
            }
        }
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, username, master_password, phone, phone_verified FROM users WHERE username=?")) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("master_password"),
                            rs.getString("phone"),
                            rs.getBoolean("phone_verified")));
                }
            }
        }
    }

    public UserDao() {}
}

package vault.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import vault.db.DBConnection;
import vault.model.VaultEntry;
import vault.model.VaultEntryExport;

public final class VaultEntryDao {

    public int insert(int userId, String site, String loginUser, String encryptedPass,
                      String category, String notes) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    """
                        INSERT INTO vault_entries(user_id, site_name, login_username, encrypted_password, category, notes)
                        VALUES (?, ?, ?, ?, ?, ?)
                        """,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, userId);
                ps.setString(2, site);
                ps.setString(3, loginUser);
                ps.setString(4, encryptedPass);
                ps.setString(5, category != null && !category.isBlank() ? category : null);
                ps.setString(6, notes != null && !notes.isBlank() ? notes : null);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                    throw new SQLException("No generated key returned");
                }
            }
        }
    }

    public boolean existsDuplicate(int userId, String site, String loginUser) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM vault_entries WHERE user_id=? AND site_name=? AND login_username=?")) {
                ps.setInt(1, userId);
                ps.setString(2, site);
                ps.setString(3, loginUser);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        }
    }

    public List<VaultEntry> findAllByUserId(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, user_id, site_name, login_username, encrypted_password, category, notes, created_at, last_accessed " +
                    "FROM vault_entries WHERE user_id=? ORDER BY site_name, login_username")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<VaultEntry> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return list;
                }
            }
        }
    }

    public List<VaultEntry> searchBySite(int userId, String sitePattern) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, user_id, site_name, login_username, encrypted_password, category, notes, created_at, last_accessed " +
                    "FROM vault_entries WHERE user_id=? AND site_name LIKE ? ORDER BY site_name, login_username")) {
                ps.setInt(1, userId);
                ps.setString(2, "%" + sitePattern + "%");
                try (ResultSet rs = ps.executeQuery()) {
                    List<VaultEntry> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(mapRow(rs));
                    }
                    return list;
                }
            }
        }
    }

    public Optional<VaultEntry> findById(int id, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id, user_id, site_name, login_username, encrypted_password, category, notes, created_at, last_accessed " +
                    "FROM vault_entries WHERE id=? AND user_id=?")) {
                ps.setInt(1, id);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    return Optional.of(mapRow(rs));
                }
            }
        }
    }

    public int update(int id, int userId, String loginUser, String encryptedPass) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE vault_entries SET login_username=?, encrypted_password=? WHERE id=? AND user_id=?")) {
                ps.setString(1, loginUser);
                ps.setString(2, encryptedPass);
                ps.setInt(3, id);
                ps.setInt(4, userId);
                return ps.executeUpdate();
            }
        }
    }

    public int delete(int id, int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM vault_entries WHERE id=? AND user_id=?")) {
                ps.setInt(1, id);
                ps.setInt(2, userId);
                return ps.executeUpdate();
            }
        }
    }

    public void updateLastAccessed(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE vault_entries SET last_accessed = CURRENT_TIMESTAMP WHERE id=?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
    }

    public List<VaultEntryExport> findAllForExport(int userId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) throw new SQLException("Connection is null");
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT site_name, login_username, encrypted_password FROM vault_entries WHERE user_id=?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<VaultEntryExport> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new VaultEntryExport(
                                rs.getString("site_name"),
                                rs.getString("login_username"),
                                rs.getString("encrypted_password")));
                    }
                    return list;
                }
            }
        }
    }

    private static VaultEntry mapRow(ResultSet rs) throws SQLException {
        return new VaultEntry(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("site_name"),
                rs.getString("login_username"),
                rs.getString("encrypted_password"),
                rs.getString("category"),
                rs.getString("notes"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("last_accessed"));
    }

    public VaultEntryDao() {}
}

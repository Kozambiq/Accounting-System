package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Persisted notifications for the dashboard. Survive logout and re-login.
 */
public final class NotificationRepository {

    private NotificationRepository() {}

    public static final class NotificationEntry {
        public final String message;
        public final String createdAt;

        public NotificationEntry(String message, String createdAt) {
            this.message = message != null ? message : "";
            this.createdAt = createdAt != null ? createdAt : "";
        }
    }

    public static void insert(Integer userId, String message) {
        if (userId == null || message == null || message.isBlank()) return;
        String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Most recent first. */
    public static List<NotificationEntry> getAll(Integer userId) {
        List<NotificationEntry> result = new ArrayList<>();
        if (userId == null) return result;
        String sql = "SELECT message, created_at FROM notifications WHERE user_id = ? ORDER BY id DESC";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new NotificationEntry(rs.getString("message"), rs.getString("created_at")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void clearAll(Integer userId) {
        if (userId == null) return;
        String sql = "DELETE FROM notifications WHERE user_id = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

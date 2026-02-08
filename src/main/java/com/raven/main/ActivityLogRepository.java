package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs and retrieves user-specific activities for the dashboard Recent Activity panel.
 */
public final class ActivityLogRepository {

    private ActivityLogRepository() {}

    /** activity_type: add, edit, delete, remove, generate */
    public static void log(String activityType, String entityType, String description) {
        Integer userId = Session.getUserId();
        if (userId == null) return;
        String sql = "INSERT INTO activity_log (user_id, activity_type, entity_type, description) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, activityType);
            ps.setString(3, entityType);
            ps.setString(4, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static final class ActivityEntry {
        public final String activityType;
        public final String entityType;
        public final String description;
        public final String createdAt;

        public ActivityEntry(String activityType, String entityType, String description, String createdAt) {
            this.activityType = activityType;
            this.entityType = entityType;
            this.description = description;
            this.createdAt = createdAt != null ? createdAt : "";
        }
    }

    /** Returns most recent activities for the current user (limit 10). */
    public static List<ActivityEntry> getRecent(int limit) {
        List<ActivityEntry> result = new ArrayList<>();
        Integer userId = Session.getUserId();
        if (userId == null) return result;
        String sql = "SELECT activity_type, entity_type, description, created_at FROM activity_log WHERE user_id = ? ORDER BY id DESC LIMIT ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new ActivityEntry(
                            rs.getString("activity_type"),
                            rs.getString("entity_type"),
                            rs.getString("description"),
                            rs.getString("created_at")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}

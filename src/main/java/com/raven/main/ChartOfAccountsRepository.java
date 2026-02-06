package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Data-access methods for the Chart of Accounts table.
 *
 * All queries are scoped to the currently logged-in user via {@code user_id}.
 */
public class ChartOfAccountsRepository {

    /**
     * Check whether an account name already exists for the current user.
     * The name passed in must already be normalized to UPPERCASE.
     */
    public boolean accountNameExistsForCurrentUser(String normalizedAccountName) throws SQLException {
        Integer userId = Session.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }

        String sql = "SELECT 1 FROM Chart_of_Accounts WHERE user_id = ? AND account_name = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, normalizedAccountName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Insert a new chart-of-accounts row for the current user.
     * The account name must already be normalized to UPPERCASE.
     */
    public void insertAccountForCurrentUser(String normalizedAccountName, String accountType) throws SQLException {
        Integer userId = Session.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }

        // Use the system's local date/time so the stored timestamp matches
        // the computer clock rather than SQLite's UTC CURRENT_TIMESTAMP.
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = now.format(formatter);

        String sql = "INSERT INTO Chart_of_Accounts (user_id, account_name, account_type, created_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, normalizedAccountName);
            ps.setString(3, accountType);
            ps.setString(4, createdAt);
            ps.executeUpdate();
        }
    }

    /**
     * Utility to transform a stored UPPERCASE account name into Title Case
     * for display purposes, e.g. "CASH ON HAND" -> "Cash On Hand".
     */
    public static String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        boolean capitalizeNext = true;

        for (char c : lower.toCharArray()) {
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    sb.append(Character.toTitleCase(c));
                    capitalizeNext = false;
                } else {
                    sb.append(c);
                }
            } else {
                sb.append(c);
                if (Character.isWhitespace(c)) {
                    capitalizeNext = true;
                }
            }
        }

        return sb.toString();
    }
}


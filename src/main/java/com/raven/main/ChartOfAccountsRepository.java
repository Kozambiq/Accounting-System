package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Class for managing Chart of Accounts data, including checking for existing account names and inserting new accounts for the current user
public class ChartOfAccountsRepository {

    // Checks if an account name already exists for the current user in the Chart_of_Accounts.java table
    public boolean accountNameExistsForCurrentUser(String normalizedAccountName) throws SQLException {
        Integer userId = Session.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }

        // Check if the account name already exists for the current user in the database
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

    // Inserts a new account for the current user with the given account name and account type along with a timestamp of when it was created
    public void insertAccountForCurrentUser(String normalizedAccountName, String accountType) throws SQLException {
        Integer userId = Session.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }

        // Get the current timestamp in the format "yyyy-MM-dd HH:mm:ss" to store in the created_at column of the database
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = now.format(formatter);

        // Insert the new account for the current user into the Chart_of_Accounts table with the provided account name, account type, and created_at timestamp
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

    // Utility method to convert a string to title case for consistent account name formatting
    public static String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        // Convert the input string to lowercase and then capitalize the first letter of each word while keeping the rest of the letters in lowercase
        String lower = value.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder(lower.length());
        boolean capitalizeNext = true;

        // Iterate through each character in the lowercase string and build it by capitalizing the first letter of each word and keeping the rest of the letters in lowercase
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


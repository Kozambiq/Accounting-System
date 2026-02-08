package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Repository for saving and loading journal entries consisting of
 * a single header and multiple detail lines.
 */
public class JournalEntryRepository {

    public static class JournalLine {
        public final String accountName;
        public final double debit;
        public final double credit;

        public JournalLine(String accountName, double debit, double credit) {
            this.accountName = accountName;
            this.debit = debit;
            this.credit = credit;
        }
    }

    public static class JournalEntry {
        public final int id;
        public final String entryName;
        public final String createdAt;
        public final List<JournalLine> lines;

        public JournalEntry(int id, String entryName, String createdAt, List<JournalLine> lines) {
            this.id = id;
            this.entryName = entryName != null ? entryName : "";
            this.createdAt = createdAt;
            this.lines = lines;
        }
    }

    /**
     * Save a journal entry header and its lines for the current user.
     *
     * @param entryName display name for the entry (will be stored in Title Case)
     * @param lines     the journal lines
     * @return the generated header id
     */
    public int saveJournalEntry(String entryName, List<JournalLine> lines) throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Journal entry must have at least one line.");
        }

        String entryNameTitleCase = entryName != null && !entryName.isBlank()
                ? ChartOfAccountsRepository.toTitleCase(entryName.trim()) : "";

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = now.format(formatter);

        int headerId;
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                String insertHeader = """
                        INSERT INTO journal_entry_headers (user_id, entry_name, created_at)
                        VALUES (?, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(insertHeader)) {
                    ps.setInt(1, userId);
                    ps.setString(2, entryNameTitleCase);
                    ps.setString(3, createdAt);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("SELECT last_insert_rowid()")) {
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Failed to retrieve journal header id.");
                        }
                        headerId = rs.getInt(1);
                    }
                }

                String insertLine = """
                        INSERT INTO journal_entry_lines (header_id, account_name, debit, credit)
                        VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(insertLine)) {
                    for (JournalLine line : lines) {
                        ps.setInt(1, headerId);
                        ps.setString(2, line.accountName);
                        ps.setDouble(3, line.debit);
                        ps.setDouble(4, line.credit);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        return headerId;
    }

    /**
     * Load recent journal entries for the current user, including their lines.
     */
    public List<JournalEntry> loadJournalEntriesForCurrentUser() throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }

        List<JournalEntry> result = new ArrayList<>();

        String headerSql = """
                SELECT id, entry_name, created_at
                  FROM journal_entry_headers
                 WHERE user_id = ?
                 ORDER BY id DESC
                """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement psHeader = conn.prepareStatement(headerSql)) {
            psHeader.setInt(1, userId);
            try (ResultSet rsHeader = psHeader.executeQuery()) {
                while (rsHeader.next()) {
                    int headerId = rsHeader.getInt("id");
                    String entryName = rsHeader.getString("entry_name");
                    String createdAt = rsHeader.getString("created_at");
                    List<JournalLine> lines = new ArrayList<>();

                    String lineSql = """
                            SELECT account_name, debit, credit
                              FROM journal_entry_lines
                             WHERE header_id = ?
                             ORDER BY id ASC
                            """;
                    try (PreparedStatement psLine = conn.prepareStatement(lineSql)) {
                        psLine.setInt(1, headerId);
                        try (ResultSet rsLine = psLine.executeQuery()) {
                            while (rsLine.next()) {
                                String accountName = rsLine.getString("account_name");
                                double debit = rsLine.getDouble("debit");
                                double credit = rsLine.getDouble("credit");
                                lines.add(new JournalLine(accountName, debit, credit));
                            }
                        }
                    }

                    result.add(new JournalEntry(headerId, entryName, createdAt, lines));
                }
            }
        }

        return result;
    }

    /**
     * Load a single journal entry by header id. Returns null if not found or
     * the entry does not belong to the current user.
     */
    public JournalEntry loadById(int headerId) throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) return null;

        String headerSql = """
                SELECT id, entry_name, created_at
                  FROM journal_entry_headers
                 WHERE id = ? AND user_id = ?
                """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(headerSql)) {
            ps.setInt(1, headerId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String entryName = rs.getString("entry_name");
                String createdAt = rs.getString("created_at");
                List<JournalLine> lines = new ArrayList<>();
                String lineSql = """
                        SELECT account_name, debit, credit
                          FROM journal_entry_lines
                         WHERE header_id = ?
                         ORDER BY id ASC
                        """;
                try (PreparedStatement psLine = conn.prepareStatement(lineSql)) {
                    psLine.setInt(1, headerId);
                    try (ResultSet rsLine = psLine.executeQuery()) {
                        while (rsLine.next()) {
                            lines.add(new JournalLine(
                                    rsLine.getString("account_name"),
                                    rsLine.getDouble("debit"),
                                    rsLine.getDouble("credit")));
                        }
                    }
                }
                return new JournalEntry(headerId, entryName, createdAt, lines);
            }
        }
    }

    /**
     * Update journal entry header (entry_name) and lines for the given header. The header must belong to the current user.
     * @param entryName display name (will be stored in Title Case); null/blank uses existing value
     */
    public void updateJournalEntry(int headerId, String entryName, List<JournalLine> lines) throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) throw new IllegalStateException("No logged-in user.");
        if (lines == null || lines.isEmpty()) throw new IllegalArgumentException("At least one line required.");

        String entryNameTitleCase = entryName != null && !entryName.isBlank()
                ? ChartOfAccountsRepository.toTitleCase(entryName.trim()) : null;

        try (Connection conn = DBConnection.connect()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM journal_entry_headers WHERE id = ? AND user_id = ?")) {
                check.setInt(1, headerId);
                check.setInt(2, userId);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Journal entry not found or access denied.");
                }
            }
            conn.setAutoCommit(false);
            try {
                if (entryNameTitleCase != null) {
                    try (PreparedStatement upd = conn.prepareStatement(
                            "UPDATE journal_entry_headers SET entry_name = ? WHERE id = ? AND user_id = ?")) {
                        upd.setString(1, entryNameTitleCase);
                        upd.setInt(2, headerId);
                        upd.setInt(3, userId);
                        upd.executeUpdate();
                    }
                }
                try (PreparedStatement del = conn.prepareStatement("DELETE FROM journal_entry_lines WHERE header_id = ?")) {
                    del.setInt(1, headerId);
                    del.executeUpdate();
                }
                String insertLine = """
                        INSERT INTO journal_entry_lines (header_id, account_name, debit, credit)
                        VALUES (?, ?, ?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(insertLine)) {
                    for (JournalLine line : lines) {
                        ps.setInt(1, headerId);
                        ps.setString(2, line.accountName);
                        ps.setDouble(3, line.debit);
                        ps.setDouble(4, line.credit);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Delete a journal entry (header and all lines). Only allowed if the header belongs to the current user.
     */
    public void deleteJournalEntry(int headerId) throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) throw new IllegalStateException("No logged-in user.");

        try (Connection conn = DBConnection.connect()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM journal_entry_headers WHERE id = ? AND user_id = ?")) {
                ps.setInt(1, headerId);
                ps.setInt(2, userId);
                int n = ps.executeUpdate();
                if (n == 0) throw new SQLException("Journal entry not found or access denied.");
            }
        }
        // Lines are deleted by FK CASCADE
    }

    /**
     * Returns the set of account names (normalized: uppercase, trimmed) that appear in
     * journal_entry_lines for the given user's headers. Used to determine "posted" accounts.
     */
    public static Set<String> getPostedAccountNamesNormalized(Integer userId) {
        Set<String> out = new HashSet<>();
        if (userId == null) return out;
        String sql = """
                SELECT DISTINCT TRIM(UPPER(l.account_name)) AS n
                  FROM journal_entry_lines l
                  JOIN journal_entry_headers h ON l.header_id = h.id
                 WHERE h.user_id = ?
                """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String n = rs.getString("n");
                    if (n != null && !n.isEmpty()) out.add(n);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    /**
     * After renaming an account in Chart_of_Accounts, propagate the new name to all
     * journal_entry_lines for this user so Journal Entries, Ledger, Trial Balance, and
     * Financial Reports stay consistent. oldNormalized and newNormalized should be
     * uppercase trimmed (same format as stored in CoA and in lines).
     */
    public static void updateAccountNameInLines(Integer userId, String oldNormalized, String newNormalized) {
        if (userId == null || oldNormalized == null || newNormalized == null) return;
        if (oldNormalized.equals(newNormalized)) return;
        String sql = """
                UPDATE journal_entry_lines
                   SET account_name = ?
                 WHERE header_id IN (SELECT id FROM journal_entry_headers WHERE user_id = ?)
                   AND TRIM(UPPER(account_name)) = ?
                """;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newNormalized);
            ps.setInt(2, userId);
            ps.setString(3, oldNormalized.trim().toUpperCase());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


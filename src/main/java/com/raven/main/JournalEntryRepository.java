package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
        public final String createdAt;
        public final List<JournalLine> lines;

        public JournalEntry(int id, String createdAt, List<JournalLine> lines) {
            this.id = id;
            this.createdAt = createdAt;
            this.lines = lines;
        }
    }

    /**
     * Save a journal entry header and its lines for the current user.
     *
     * @return the generated header id
     */
    public int saveJournalEntry(List<JournalLine> lines) throws SQLException {
        Integer userId = Session.getUserId();
        if (userId == null) {
            throw new IllegalStateException("No logged-in user in Session.");
        }
        if (lines == null || lines.isEmpty()) {
            throw new IllegalArgumentException("Journal entry must have at least one line.");
        }

        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = now.format(formatter);

        int headerId;
        try (Connection conn = DBConnection.connect()) {
            conn.setAutoCommit(false);
            try {
                String insertHeader = """
                        INSERT INTO journal_entry_headers (user_id, created_at)
                        VALUES (?, ?)
                        """;
                try (PreparedStatement ps = conn.prepareStatement(insertHeader)) {
                    ps.setInt(1, userId);
                    ps.setString(2, createdAt);
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
                SELECT id, created_at
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

                    result.add(new JournalEntry(headerId, createdAt, lines));
                }
            }
        }

        return result;
    }
}


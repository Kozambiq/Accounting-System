package com.raven.main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Computes total assets, liabilities, and equity from Chart_of_Accounts and journal entries
 * for the current user (all-time balances). Used by dashboard metric cards.
 */
public final class DashboardMetrics {

    private DashboardMetrics() {}

    /** Returns { totalAssets, totalLiabilities, totalEquity }. Equity is not derived; sum of equity account balances. */
    public static double[] computeTotals() {
        double totalAssets = 0, totalLiabilities = 0, totalEquity = 0;
        Integer userId = Session.getUserId();
        if (userId == null) return new double[]{0, 0, 0};

        try (Connection conn = DBConnection.connect()) {
            String accountsSql = "SELECT account_name, account_type FROM Chart_of_Accounts WHERE user_id = ?";
            try (PreparedStatement psAcc = conn.prepareStatement(accountsSql)) {
                psAcc.setInt(1, userId);
                try (ResultSet rsAcc = psAcc.executeQuery()) {
                    while (rsAcc.next()) {
                        String accName = rsAcc.getString("account_name");
                        String accType = rsAcc.getString("account_type");
                        double balance = getAccountBalance(conn, userId, accName, accType);
                        String u = (accType != null ? accType : "").trim().toUpperCase();
                        if ("ASSET".equals(u) || "ASSETS".equals(u)) {
                            totalAssets += Math.abs(balance);
                        } else if ("LIABILITY".equals(u) || "LIABILITIES".equals(u)) {
                            totalLiabilities += Math.abs(balance);
                        } else if ("EQUITY".equals(u)) {
                            totalEquity += Math.abs(balance);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new double[]{totalAssets, totalLiabilities, totalEquity};
    }

    /** Total revenue from Income Statement: sum of all revenue account balances from journal entries. */
    public static double computeTotalRevenue() {
        double total = 0;
        Integer userId = Session.getUserId();
        if (userId == null) return 0;
        try (Connection conn = DBConnection.connect()) {
            String sql = "SELECT account_name, account_type FROM Chart_of_Accounts WHERE user_id = ?";
            try (PreparedStatement psAcc = conn.prepareStatement(sql)) {
                psAcc.setInt(1, userId);
                try (ResultSet rsAcc = psAcc.executeQuery()) {
                    while (rsAcc.next()) {
                        String accName = rsAcc.getString("account_name");
                        String accType = rsAcc.getString("account_type");
                        String u = (accType != null ? accType : "").trim().toUpperCase();
                        if ("REVENUE".equals(u) || "REVENUES".equals(u)) {
                            total += Math.abs(getAccountBalance(conn, userId, accName, accType));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return total;
    }

    private static double getAccountBalance(Connection conn, int userId, String accountName, String accountType) throws SQLException {
        String sql = """
                SELECT l.debit, l.credit FROM journal_entry_lines l
                JOIN journal_entry_headers h ON l.header_id = h.id
                WHERE h.user_id = ? AND UPPER(TRIM(l.account_name)) = UPPER(TRIM(?))
                """;
        double balance = 0;
        boolean isAssetOrExpense = "ASSET".equalsIgnoreCase(accountType) || "EXPENSE".equalsIgnoreCase(accountType) || "EXPENSES".equalsIgnoreCase(accountType);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, accountName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double d = rs.getDouble("debit");
                    double c = rs.getDouble("credit");
                    if (isAssetOrExpense) balance += d - c;
                    else balance += c - d;
                }
            }
        }
        return balance;
    }
}

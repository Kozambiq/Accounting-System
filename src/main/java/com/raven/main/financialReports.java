package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Financial Reports: Income Statement, Cash Flow Statement, Balance Sheet.
 * Uses ledger mini cards as data source; Balance Sheet can filter by end date.
 * Cash Flow Statement categorizes by transaction counterpart (journal entries) for accuracy.
 */
public class financialReports extends JFrame {

    private Font workSansBold;
    private final ledger ledgerFrame;
    private JPanel reportContainer;
    private RoundedCardPanel reportCard;
    private LocalDate balanceSheetEndDate = null;

    public financialReports(ledger ledgerFrame) {
        this.ledgerFrame = ledgerFrame;
        setTitle("ACCOUNTING SYSTEM - Financial Reports");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        initUI();
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xE6E6EB));

        JPanel sideNav = createSideNav();
        JPanel mainContent = createMainContent();

        root.add(sideNav, BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSideNav() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(0xcdc6c6));
        side.setLayout(new BorderLayout());

        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0xcdc6c6));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("ACCOUNTING SYSTEM");
        appName.setFont(getWorkSansBold(16f));
        appName.setForeground(new Color(0x2e6417));
        logoPanel.add(appName);

        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(0xcdc6c6));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        menuPanel.add(createNavItem("DashBoard", false, "/icon/dashboard.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Chart of Accounts", false, "/icon/chart_of_accounts_icon.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Journal Entry", false, "/icon/journal_entry.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Ledger", false, "/icon/ledger.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Trial Balance", false, "/icon/trial_balance.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Financial Reports", true, "/icon/financial_report.png"));

        side.add(logoPanel, BorderLayout.NORTH);
        side.add(menuPanel, BorderLayout.CENTER);
        return side;
    }

    private JComponent createNavItem(String text, boolean selected, String iconPath) {
        Color activeColor = new Color(0xB9FF7F);
        Color inactiveColor = new Color(0xcdc6c6);
        Color baseBackground = selected ? activeColor : inactiveColor;

        RoundedNavItemPanel item = new RoundedNavItemPanel(baseBackground, activeColor, selected);
        item.setLayout(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        item.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                    JLabel iconLabel = new JLabel(new ImageIcon(img));
                    iconLabel.setOpaque(false);
                    iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
                    contentPanel.add(iconLabel);
                    contentPanel.add(Box.createHorizontalStrut(12));
                }
            } catch (Exception e) {
                System.err.println("Icon not found: " + iconPath);
            }
        }

        JLabel label = new JLabel(text);
        label.setFont(getWorkSansBold(14f));
        label.setForeground(new Color(0x2e6417));
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        contentPanel.add(label);

        item.add(contentPanel, BorderLayout.CENTER);
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { item.setHovered(true); item.repaint(); }
            @Override
            public void mouseExited(MouseEvent e) { item.setHovered(false); item.repaint(); }
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(item);
                if (window instanceof AppWindow appWindow) {
                    if ("DashBoard".equals(text)) appWindow.showDashboard();
                    else if ("Chart of Accounts".equals(text)) appWindow.showChartOfAccounts();
                    else if ("Journal Entry".equals(text)) appWindow.showJournalEntry();
                    else if ("Ledger".equals(text)) appWindow.showLedger();
                    else if ("Trial Balance".equals(text)) appWindow.showTrialBalance();
                    else if ("Financial Reports".equals(text)) appWindow.showFinancialReports();
                }
            }
        });

        return item;
    }

    private Font getWorkSansBold(float size) {
        if (workSansBold != null) return workSansBold.deriveFont(Font.BOLD, size);
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xE6E6EB));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Financial Reports");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0x2e6417));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(title);
        stack.add(Box.createVerticalStrut(16));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        buttonPanel.setOpaque(false);

        RoundedButton incomeBtn = new RoundedButton("Income Statement");
        incomeBtn.setBackground(new Color(0x2e6417));
        incomeBtn.setForeground(Color.WHITE);
        incomeBtn.addActionListener(e -> generateIncomeStatement());

        RoundedButton cashFlowBtn = new RoundedButton("Cash Flow Statement");
        cashFlowBtn.setBackground(new Color(0x2e6417));
        cashFlowBtn.setForeground(Color.WHITE);
        cashFlowBtn.addActionListener(e -> generateCashFlowStatement());

        RoundedButton balanceSheetBtn = new RoundedButton("Balance Sheet");
        balanceSheetBtn.setBackground(new Color(0x2e6417));
        balanceSheetBtn.setForeground(Color.WHITE);
        balanceSheetBtn.addActionListener(e -> showBalanceSheetDateDialog());

        buttonPanel.add(incomeBtn);
        buttonPanel.add(cashFlowBtn);
        buttonPanel.add(balanceSheetBtn);
        stack.add(buttonPanel);
        stack.add(Box.createVerticalStrut(16));

        main.add(stack, BorderLayout.NORTH);

        reportCard = new RoundedCardPanel(new Color(0xcdc6c6));
        reportCard.setLayout(new BorderLayout());
        reportCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        reportContainer = new JPanel(new BorderLayout());
        reportContainer.setOpaque(false);
        reportCard.add(reportContainer, BorderLayout.CENTER);
        main.add(reportCard, BorderLayout.CENTER);

        return main;
    }

    private List<ledger.LedgerAccountBalance> getLedgerData() {
        return ledgerFrame != null ? ledgerFrame.getLedgerDataForTrialBalance() : new ArrayList<>();
    }

    private static String formatNum(double v) {
        return new DecimalFormat("#,##0.00").format(Math.abs(v));
    }

    private void generateIncomeStatement() {
        List<ledger.LedgerAccountBalance> data = getLedgerData();

        double totalRevenue = 0, totalExpense = 0;
        List<String[]> revenueRows = new ArrayList<>();
        List<String[]> expenseRows = new ArrayList<>();

        String revUpper = "REVENUE";
        String revAlt = "REVENUES";
        String expUpper = "EXPENSE";
        String expAlt = "EXPENSES";

        for (ledger.LedgerAccountBalance item : data) {
            String type = (item.accountType != null ? item.accountType : "").toUpperCase();
            double amt = item.balance;
            if (type.equals(revUpper) || type.equals(revAlt)) {
                totalRevenue += amt;
                revenueRows.add(new String[]{ChartOfAccountsRepository.toTitleCase(item.accountName), formatNum(amt)});
            } else if (type.equals(expUpper) || type.equals(expAlt)) {
                totalExpense += Math.abs(amt);
                expenseRows.add(new String[]{ChartOfAccountsRepository.toTitleCase(item.accountName), formatNum(Math.abs(amt))});
            }
        }

        double netIncome = totalRevenue - totalExpense;

        reportContainer.removeAll();
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        north.setOpaque(false);
        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.setBackground(new Color(0x2e6417));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> generateIncomeStatement());
        north.add(refreshBtn);
        content.add(north, BorderLayout.NORTH);

        String[] cols = {"Account Name", "Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        model.addRow(new String[]{"Revenue", ""});
        for (String[] row : revenueRows) model.addRow(row);
        model.addRow(new String[]{"Total Revenue", formatNum(totalRevenue)});
        model.addRow(new String[]{});
        model.addRow(new String[]{"Expenses", ""});
        for (String[] row : expenseRows) model.addRow(row);
        model.addRow(new String[]{"Total Expenses", formatNum(totalExpense)});
        model.addRow(new String[]{});
        model.addRow(new String[]{"Net Income", formatNum(netIncome)});

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(1).setCellRenderer(rightAlign);

        int netIncomeRow = model.getRowCount() - 1;
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (row == netIncomeRow) {
                    c.setBackground(new Color(0xB9FF7F));
                    c.setForeground(new Color(0x2e6417));
                }
                return c;
            }
        });
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (row == netIncomeRow) {
                    c.setBackground(new Color(0xB9FF7F));
                    c.setForeground(new Color(0x2e6417));
                } else if (c instanceof JLabel jl) jl.setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        reportContainer.add(content, BorderLayout.CENTER);
        reportContainer.revalidate();
        reportContainer.repaint();
    }

    /** Activity categories for Cash Flow Statement. */
    private static final String OPERATING = "Operating Activities";
    private static final String INVESTING = "Investing Activities";
    private static final String FINANCING = "Financing Activities";

    /** Returns true if the account is cash-related (Cash, Cash Equivalents). No guessing—strict name check. */
    private static boolean isCashRelatedAccount(String accountName) {
        if (accountName == null) return false;
        String upper = accountName.trim().toUpperCase();
        return upper.contains("CASH") || upper.contains("CASH EQUIVALENT");
    }

    /**
     * Deterministic mapping: Chart_of_Accounts account_type → Cash Flow category.
     * Operating: Revenue/Expense. Investing: Asset (non-cash). Financing: Liability/Equity.
     */
    private static String mapAccountTypeToCategory(String accountType) {
        if (accountType == null) return OPERATING;
        String u = accountType.trim().toUpperCase();
        if ("REVENUE".equals(u) || "REVENUES".equals(u) || "EXPENSE".equals(u) || "EXPENSES".equals(u)) return OPERATING;
        if ("ASSET".equals(u) || "ASSETS".equals(u)) return INVESTING;
        if ("LIABILITY".equals(u) || "LIABILITIES".equals(u) || "EQUITY".equals(u)) return FINANCING;
        return OPERATING; // deterministic fallback: unknown type → Operating
    }

    /**
     * Get account_type from Chart_of_Accounts for the given account name (case-insensitive match).
     */
    private static String getAccountTypeFromCoA(Connection conn, int userId, String accountName) throws java.sql.SQLException {
        if (accountName == null) return null;
        String sql = "SELECT account_type FROM Chart_of_Accounts WHERE user_id = ? AND TRIM(UPPER(account_name)) = TRIM(UPPER(?))";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, accountName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("account_type") : null;
            }
        }
    }

    /**
     * Compute Cash Flow by category from journal entries. Uses only ledger mini cards to determine
     * which accounts are cash-related; then uses DB journal lines to assign each cash movement
     * to Operating/Investing/Financing based on the counterpart account type (deterministic).
     */
    private double[] computeCashFlowFromJournalEntries() {
        double opInflow = 0, opOutflow = 0, invInflow = 0, invOutflow = 0, finInflow = 0, finOutflow = 0;
        Integer userId = Session.getUserId();
        if (userId == null) return new double[]{opInflow, opOutflow, invInflow, invOutflow, finInflow, finOutflow};

        // Cash account names from ledger mini cards only (source of truth)
        Set<String> cashAccountNamesNormalized = new HashSet<>();
        for (ledger.LedgerAccountBalance item : getLedgerData()) {
            if (isCashRelatedAccount(item.accountName)) {
                cashAccountNamesNormalized.add((item.accountName != null ? item.accountName : "").trim().toUpperCase());
            }
        }
        if (cashAccountNamesNormalized.isEmpty()) return new double[]{0, 0, 0, 0, 0, 0};

        try (Connection conn = DBConnection.connect()) {
            String headerSql = "SELECT id FROM journal_entry_headers WHERE user_id = ? ORDER BY id ASC";
            try (PreparedStatement psHeader = conn.prepareStatement(headerSql)) {
                psHeader.setInt(1, userId);
                try (ResultSet rsHeader = psHeader.executeQuery()) {
                    while (rsHeader.next()) {
                        int headerId = rsHeader.getInt("id");
                        String lineSql = "SELECT account_name, debit, credit FROM journal_entry_lines WHERE header_id = ? ORDER BY id ASC";
                        try (PreparedStatement psLine = conn.prepareStatement(lineSql)) {
                            psLine.setInt(1, headerId);
                            try (ResultSet rsLine = psLine.executeQuery()) {
                                List<String> nonCashNames = new ArrayList<>();
                                List<Double> nonCashAmounts = new ArrayList<>();
                                double entryCashInflow = 0, entryCashOutflow = 0;

                                while (rsLine.next()) {
                                    String accName = rsLine.getString("account_name");
                                    String accNorm = (accName != null ? accName : "").trim().toUpperCase();
                                    double debit = rsLine.getDouble("debit");
                                    double credit = rsLine.getDouble("credit");

                                    if (cashAccountNamesNormalized.contains(accNorm)) {
                                        entryCashInflow += debit;
                                        entryCashOutflow += credit;
                                    } else {
                                        nonCashNames.add(accName);
                                        nonCashAmounts.add(Math.abs(debit) + Math.abs(credit));
                                    }
                                }

                                if (entryCashInflow == 0 && entryCashOutflow == 0) continue;

                                double totalNonCashAmount = 0;
                                for (Double a : nonCashAmounts) totalNonCashAmount += a;

                                if (totalNonCashAmount <= 0) {
                                    // Transfer between cash accounts only: allocate to Operating with net 0
                                    opInflow += entryCashInflow;
                                    opOutflow += entryCashOutflow;
                                    continue;
                                }

                                double opProp = 0, invProp = 0, finProp = 0;
                                for (int i = 0; i < nonCashNames.size(); i++) {
                                    String type = getAccountTypeFromCoA(conn, userId, nonCashNames.get(i));
                                    String cat = mapAccountTypeToCategory(type);
                                    double prop = nonCashAmounts.get(i) / totalNonCashAmount;
                                    switch (cat) {
                                        case OPERATING -> opProp += prop;
                                        case INVESTING -> invProp += prop;
                                        case FINANCING -> finProp += prop;
                                        default -> opProp += prop;
                                    }
                                }

                                opInflow += entryCashInflow * opProp;
                                opOutflow += entryCashOutflow * opProp;
                                invInflow += entryCashInflow * invProp;
                                invOutflow += entryCashOutflow * invProp;
                                finInflow += entryCashInflow * finProp;
                                finOutflow += entryCashOutflow * finProp;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new double[]{opInflow, opOutflow, invInflow, invOutflow, finInflow, finOutflow};
    }

    private void generateCashFlowStatement() {
        double[] flows = computeCashFlowFromJournalEntries();
        double opInflow = flows[0], opOutflow = flows[1], invInflow = flows[2], invOutflow = flows[3], finInflow = flows[4], finOutflow = flows[5];

        double opNet = opInflow - opOutflow;
        double invNet = invInflow - invOutflow;
        double finNet = finInflow - finOutflow;
        double totalNetCashFlow = opNet + invNet + finNet;

        reportContainer.removeAll();
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        north.setOpaque(false);
        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.setBackground(new Color(0x2e6417));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> generateCashFlowStatement());
        north.add(refreshBtn);
        content.add(north, BorderLayout.NORTH);

        String[] cols = {"Category", "Net Cash Flow"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        // Display only positive numbers: amount column is always positive; inflow/outflow in category label
        model.addRow(new String[]{categoryLabel(OPERATING, opNet), formatNum(Math.abs(opNet))});
        model.addRow(new String[]{categoryLabel(INVESTING, invNet), formatNum(Math.abs(invNet))});
        model.addRow(new String[]{categoryLabel(FINANCING, finNet), formatNum(Math.abs(finNet))});
        model.addRow(new String[]{});
        model.addRow(new String[]{totalNetCashFlow >= 0 ? "Total Net Cash Flow (Net Inflow)" : "Total Net Cash Flow (Net Outflow)", formatNum(Math.abs(totalNetCashFlow))});

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        int totalRow = 4; // Total Net Cash Flow row index

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (row == totalRow) {
                    c.setBackground(new Color(0xB9FF7F));
                    c.setForeground(new Color(0x2e6417));
                }
                return c;
            }
        });
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (row == totalRow) {
                    c.setBackground(new Color(0xB9FF7F));
                    c.setForeground(new Color(0x2e6417));
                }
                if (c instanceof JLabel jl) jl.setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        reportContainer.add(content, BorderLayout.CENTER);
        reportContainer.revalidate();
        reportContainer.repaint();
    }

    /** Category label with inflow/outflow so amount column stays positive-only. */
    private static String categoryLabel(String category, double net) {
        return net >= 0 ? category + " (Net Inflow)" : category + " (Net Outflow)";
    }

    private void showBalanceSheetDateDialog() {
        JDialog dialog = new JDialog(this, "Balance Sheet – End Date", true);
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel label = new JLabel("Select end date for balance sheet:");
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        center.add(label);
        center.add(dateSpinner);
        root.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        RoundedButton okBtn = new RoundedButton("Generate");
        okBtn.setBackground(new Color(0x2e6417));
        okBtn.setForeground(Color.WHITE);
        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        okBtn.addActionListener(e -> {
            balanceSheetEndDate = ((java.util.Date) dateSpinner.getValue()).toInstant()
                    .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            dialog.dispose();
            generateBalanceSheet(balanceSheetEndDate);
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        south.add(okBtn);
        south.add(cancelBtn);
        root.add(south, BorderLayout.SOUTH);

        dialog.add(root);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void generateBalanceSheet(LocalDate asOfDate) {
        List<BalanceSheetItem> items = getBalancesAsOfDate(asOfDate);

        double totalAssets = 0, totalLiabilities = 0, totalEquity = 0;
        List<String[]> assetRows = new ArrayList<>();
        List<String[]> liabilityRows = new ArrayList<>();
        List<String[]> equityRows = new ArrayList<>();

        for (BalanceSheetItem it : items) {
            String type = (it.accountType != null ? it.accountType : "").toUpperCase();
            double amt = Math.abs(it.balance);
            if (type.equals("ASSET") || type.equals("ASSETS")) {
                totalAssets += amt;
                assetRows.add(new String[]{ChartOfAccountsRepository.toTitleCase(it.accountName), formatNum(amt)});
            } else if (type.equals("LIABILITY") || type.equals("LIABILITIES")) {
                totalLiabilities += amt;
                liabilityRows.add(new String[]{ChartOfAccountsRepository.toTitleCase(it.accountName), formatNum(amt)});
            } else if (type.equals("EQUITY")) {
                totalEquity += amt;
                equityRows.add(new String[]{ChartOfAccountsRepository.toTitleCase(it.accountName), formatNum(amt)});
            }
        }

        reportContainer.removeAll();
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel north = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        north.setOpaque(false);
        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.setBackground(new Color(0x2e6417));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.addActionListener(e -> generateBalanceSheet(balanceSheetEndDate != null ? balanceSheetEndDate : LocalDate.now()));
        north.add(refreshBtn);
        content.add(north, BorderLayout.NORTH);

        String[] cols = {"Account Name", "Amount"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        model.addRow(new String[]{"Assets", ""});
        for (String[] row : assetRows) model.addRow(row);
        model.addRow(new String[]{"Total Assets", formatNum(totalAssets)});
        model.addRow(new String[]{});
        model.addRow(new String[]{"Liabilities", ""});
        for (String[] row : liabilityRows) model.addRow(row);
        model.addRow(new String[]{"Total Liabilities", formatNum(totalLiabilities)});
        model.addRow(new String[]{});
        model.addRow(new String[]{"Equity", ""});
        for (String[] row : equityRows) model.addRow(row);
        model.addRow(new String[]{"Total Equity", formatNum(totalEquity)});

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        Color highlight = new Color(0xB9FF7F);
        int[] totalRows = {assetRows.size() + 2, assetRows.size() + 4 + liabilityRows.size() + 2, model.getRowCount() - 1};
        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                for (int tr : totalRows) if (row == tr) { c.setBackground(highlight); c.setForeground(new Color(0x2e6417)); break; }
                return c;
            }
        });
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                for (int tr : totalRows) if (row == tr) { c.setBackground(highlight); c.setForeground(new Color(0x2e6417)); break; }
                if (c instanceof JLabel jl) jl.setHorizontalAlignment(SwingConstants.RIGHT);
                return c;
            }
        });

        content.add(new JScrollPane(table), BorderLayout.CENTER);
        reportContainer.add(content, BorderLayout.CENTER);
        reportContainer.revalidate();
        reportContainer.repaint();
    }

    private static class BalanceSheetItem {
        final String accountName;
        final String accountType;
        final double balance;

        BalanceSheetItem(String accountName, String accountType, double balance) {
            this.accountName = accountName;
            this.accountType = accountType;
            this.balance = balance;
        }
    }

    /** Compute balances as of given date from journal entries (user-scoped). */
    private List<BalanceSheetItem> getBalancesAsOfDate(LocalDate asOf) {
        List<BalanceSheetItem> result = new ArrayList<>();
        Integer userId = Session.getUserId();
        if (userId == null) return result;

        String dateEnd = asOf.format(DateTimeFormatter.ISO_LOCAL_DATE) + " 23:59:59";

        try (Connection conn = DBConnection.connect()) {
            String accountsSql = "SELECT account_name, account_type FROM Chart_of_Accounts WHERE user_id = ?";
            try (PreparedStatement psAcc = conn.prepareStatement(accountsSql)) {
                psAcc.setInt(1, userId);
                try (ResultSet rsAcc = psAcc.executeQuery()) {
                    while (rsAcc.next()) {
                        String accName = rsAcc.getString("account_name");
                        String accType = rsAcc.getString("account_type");
                        String sql = """
                                SELECT l.debit, l.credit FROM journal_entry_lines l
                                JOIN journal_entry_headers h ON l.header_id = h.id
                                WHERE h.user_id = ? AND UPPER(l.account_name) = UPPER(?) AND h.created_at <= ?
                                """;
                        double balance = 0;
                        boolean isAssetOrExpense = "ASSET".equalsIgnoreCase(accType) || "EXPENSE".equalsIgnoreCase(accType) || "EXPENSES".equalsIgnoreCase(accType);
                        try (PreparedStatement ps = conn.prepareStatement(sql)) {
                            ps.setInt(1, userId);
                            ps.setString(2, accName);
                            ps.setString(3, dateEnd);
                            try (ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    double d = rs.getDouble("debit");
                                    double c = rs.getDouble("credit");
                                    if (isAssetOrExpense) balance += d - c;
                                    else balance += c - d;
                                }
                            }
                        }
                        result.add(new BalanceSheetItem(accName, accType, balance));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private static class RoundedNavItemPanel extends JPanel {
        private final Color baseColor;
        private final Color hoverColor;
        private final boolean selected;
        private boolean hovered;

        RoundedNavItemPanel(Color baseColor, Color hoverColor, boolean selected) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.selected = selected;
            setOpaque(false);
        }

        void setHovered(boolean hovered) { this.hovered = hovered; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(selected || hovered ? hoverColor : baseColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedCardPanel extends JPanel {
        private final Color bgColor;

        RoundedCardPanel(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private static final int ARC = 18;

        RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground();
            if (getModel().isPressed()) bg = bg.darker();
            else if (getModel().isRollover()) bg = bg.brighter();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

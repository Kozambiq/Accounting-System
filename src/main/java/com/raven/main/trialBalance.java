package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;

/**
 * Trial Balance window.
 *
 * Left side navigation is copied from the existing design, and the center
 * content area contains:
 * - Title "Trial Balance"
 * - "Generate Trial Balance" rounded button
 * - A gray parent card that displays the trial balance table
 */
public class trialBalance extends JFrame {

    private Font workSansBold;
    private JPanel trialBalanceContainer;
    private RoundedCardPanel trialBalanceCard;
    private JTable trialBalanceTable;

    public trialBalance() {
        setTitle("ACCOUNTING SYSTEM - Trial Balance");
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

        // Left menu items: Dashboard, Chart of Accounts, Journal Entry, Ledger, Trial Balance, Financial Reports
        menuPanel.add(createNavItem("DashBoard", false, "/icon/dashboard.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Chart of Accounts", false, "/icon/chart_of_accounts_icon.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Journal Entry", false, "/icon/journal_entry.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Ledger", false, "/icon/ledger.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        // Highlight Trial Balance for this screen
        menuPanel.add(createNavItem("Trial Balance", true, "/icon/trial_balance.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Financial Reports", false, "/icon/financial_report.png"));

        side.add(logoPanel, BorderLayout.NORTH);
        side.add(menuPanel, BorderLayout.CENTER);

        return side;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xE6E6EB));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        // Title copied from homePage, text changed to "Trial Balance"
        JLabel title = new JLabel("Trial Balance");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0x2e6417));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(title);

        stack.add(Box.createVerticalStrut(16));

        // Rounded green "Generate Trial Balance" button
        RoundedButton generateBtn = new RoundedButton("Generate Trial Balance");
        generateBtn.setBackground(new Color(0x2e6417));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        generateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showGenerateTrialBalanceDialog();
            }
        });

        stack.add(generateBtn);
        stack.add(Box.createVerticalStrut(16));

        main.add(stack, BorderLayout.NORTH);

        // Full-width card container for trial balance table
        trialBalanceCard = new RoundedCardPanel(new Color(0xcdc6c6));
        trialBalanceCard.setLayout(new BorderLayout());
        trialBalanceCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        trialBalanceCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        trialBalanceContainer = new JPanel(new BorderLayout());
        trialBalanceContainer.setOpaque(false);

        trialBalanceCard.add(trialBalanceContainer, BorderLayout.CENTER);
        main.add(trialBalanceCard, BorderLayout.CENTER);

        return main;
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
            public void mouseEntered(MouseEvent e) {
                item.setHovered(true);
                item.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setHovered(false);
                item.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(item);
                if (window instanceof AppWindow appWindow) {
                    if ("DashBoard".equals(text)) {
                        appWindow.showDashboard();
                    } else if ("Chart of Accounts".equals(text)) {
                        appWindow.showChartOfAccounts();
                    } else if ("Journal Entry".equals(text)) {
                        appWindow.showJournalEntry();
                    } else if ("Ledger".equals(text)) {
                        appWindow.showLedger();
                    } else if ("Trial Balance".equals(text)) {
                        appWindow.showTrialBalance();
                    } else {
                        System.out.println("Clicked: " + text);
                    }
                } else {
                    System.out.println("Clicked: " + text);
                }
            }
        });

        return item;
    }

    private Font getWorkSansBold(float size) {
        if (workSansBold != null) {
            return workSansBold.deriveFont(Font.BOLD, size);
        }
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    // ----- Generate Trial Balance flow ---------------------------------------------

    private void showGenerateTrialBalanceDialog() {
        JDialog dialog = new JDialog(this, "Generate Trial Balance", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(700, 500));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Table with Account Name, Debit, Credit columns
        String[] cols = {"Account Name", "Debit", "Credit"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load all accounts and calculate balances
        java.util.Map<String, AccountBalance> accountBalances = calculateAccountBalances();

        double totalDebit = 0.0;
        double totalCredit = 0.0;

        for (java.util.Map.Entry<String, AccountBalance> entry : accountBalances.entrySet()) {
            String accountName = entry.getKey();
            AccountBalance balance = entry.getValue();

            double debit = 0.0;
            double credit = 0.0;

            if (balance.balance > 0) {
                debit = balance.balance;
                totalDebit += debit;
            } else if (balance.balance < 0) {
                credit = Math.abs(balance.balance);
                totalCredit += credit;
            }

            model.addRow(new Object[]{
                    ChartOfAccountsRepository.toTitleCase(accountName),
                    debit == 0 ? "" : formatNumber(debit),
                    credit == 0 ? "" : formatNumber(credit)
            });
        }

        // Add totals row
        model.addRow(new Object[]{"TOTAL", formatNumber(totalDebit), formatNumber(totalCredit)});

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Highlight totals row if debit != credit
        if (Math.abs(totalDebit - totalCredit) > 0.01) {
            table.setDefaultRenderer(Object.class, new TableCellRenderer() {
                private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                              boolean isSelected, boolean hasFocus,
                                                              int row, int column) {
                    Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row == table.getRowCount() - 1) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    }
                    return c;
                }
            });
        }
        
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        bottom.setOpaque(false);

        RoundedButton doneBtn = new RoundedButton("Done");
        doneBtn.setBackground(new Color(0x2e6417));
        doneBtn.setForeground(Color.WHITE);

        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);

        bottom.add(doneBtn);
        bottom.add(cancelBtn);
        root.add(bottom, BorderLayout.SOUTH);

        // Validation message
        if (Math.abs(totalDebit - totalCredit) > 0.01) {
            JLabel warningLabel = new JLabel(
                    "<html><font color='red'>Warning: Total Debit (" + formatNumber(totalDebit) + 
                    ") does not equal Total Credit (" + formatNumber(totalCredit) + ")</font></html>");
            warningLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
            root.add(warningLabel, BorderLayout.NORTH);
        }

        doneBtn.addActionListener(e -> {
            dialog.dispose();
            displayTrialBalance(accountBalances);
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(root, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Calculate balances for all accounts from journal entries.
     */
    private java.util.Map<String, AccountBalance> calculateAccountBalances() {
        java.util.Map<String, AccountBalance> balances = new java.util.HashMap<>();

        Integer userId = Session.getUserId();
        if (userId == null) {
            return balances;
        }

        // Get all accounts for the current user
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT id, account_name, account_type
                       FROM Chart_of_Accounts
                      WHERE user_id = ?
                     """)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String accountName = rs.getString("account_name");
                    String accountType = rs.getString("account_type");

                    // Calculate balance for this account
                    double balance = calculateBalanceForAccount(accountName, accountType, userId);

                    balances.put(accountName, new AccountBalance(accountName, accountType, balance));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return balances;
    }

    /**
     * Calculate balance for a specific account based on account type and journal entries.
     */
    private double calculateBalanceForAccount(String accountName, String accountType, int userId) {
        double balance = 0.0;

        String accountTypeUpper = accountType.toUpperCase();
        boolean isAssetOrExpense = accountTypeUpper.equals("ASSET") ||
                                   accountTypeUpper.equals("EXPENSE") ||
                                   accountTypeUpper.equals("EXPENSES");

        String sql = """
                SELECT l.debit, l.credit
                  FROM journal_entry_headers h
                  JOIN journal_entry_lines l ON l.header_id = h.id
                 WHERE h.user_id = ? AND UPPER(l.account_name) = UPPER(?)
                """;

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, accountName);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");

                    if (isAssetOrExpense) {
                        balance = balance + debit - credit;
                    } else {
                        // Liability, Equity, or Revenue
                        balance = balance + credit - debit;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return balance;
    }

    /**
     * Display the trial balance table in the main window.
     */
    private void displayTrialBalance(java.util.Map<String, AccountBalance> accountBalances) {
        trialBalanceContainer.removeAll();

        String[] cols = {"Account Name", "Debit", "Credit"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        double totalDebit = 0.0;
        double totalCredit = 0.0;

        for (java.util.Map.Entry<String, AccountBalance> entry : accountBalances.entrySet()) {
            String accountName = entry.getKey();
            AccountBalance balance = entry.getValue();

            double debit = 0.0;
            double credit = 0.0;

            if (balance.balance > 0) {
                debit = balance.balance;
                totalDebit += debit;
            } else if (balance.balance < 0) {
                credit = Math.abs(balance.balance);
                totalCredit += credit;
            }

            model.addRow(new Object[]{
                    ChartOfAccountsRepository.toTitleCase(accountName),
                    debit == 0 ? "" : formatNumber(debit),
                    credit == 0 ? "" : formatNumber(credit)
            });
        }

        // Add totals row
        model.addRow(new Object[]{"TOTAL", formatNumber(totalDebit), formatNumber(totalCredit)});

        trialBalanceTable = new JTable(model);
        trialBalanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trialBalanceTable.setFillsViewportHeight(true);
        
        // Highlight totals row if debit != credit
        if (Math.abs(totalDebit - totalCredit) > 0.01) {
            trialBalanceTable.setDefaultRenderer(Object.class, new TableCellRenderer() {
                private DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                              boolean isSelected, boolean hasFocus,
                                                              int row, int column) {
                    Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row == table.getRowCount() - 1) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    } else {
                        c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    }
                    return c;
                }
            });
        }

        JScrollPane scroll = new JScrollPane(trialBalanceTable);
        trialBalanceContainer.add(scroll, BorderLayout.CENTER);

        // Add warning label if totals don't match
        if (Math.abs(totalDebit - totalCredit) > 0.01) {
            JLabel warningLabel = new JLabel(
                    "<html><font color='red' size='+1'><b>Warning: Total Debit (" + formatNumber(totalDebit) + 
                    ") does not equal Total Credit (" + formatNumber(totalCredit) + ")</b></font></html>");
            warningLabel.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
            trialBalanceContainer.add(warningLabel, BorderLayout.NORTH);
        }

        trialBalanceContainer.revalidate();
        trialBalanceContainer.repaint();
    }

    /**
     * Format number with commas and two decimal places.
     */
    private String formatNumber(double value) {
        DecimalFormat formatter = new DecimalFormat("#,##0.00");
        return formatter.format(value);
    }

    /**
     * Inner class to hold account balance information.
     */
    private static class AccountBalance {
        final String accountName;
        final String accountType;
        final double balance;

        AccountBalance(String accountName, String accountType, double balance) {
            this.accountName = accountName;
            this.accountType = accountType;
            this.balance = balance;
        }
    }

    // ----- Shared rounded components ----------------------------------------

    // Rounded background for side-nav items (copied from existing design)
    private static class RoundedNavItemPanel extends JPanel {
        private final Color baseColor;
        private final Color hoverColor;
        private final boolean selected;
        private boolean hovered;

        public RoundedNavItemPanel(Color baseColor, Color hoverColor, boolean selected) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            this.selected = selected;
            setOpaque(false);
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = (selected || hovered) ? hoverColor : baseColor;
            g2.setColor(fill);
            int arc = 18;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded card panel used for parent/mini cards
    private static class RoundedCardPanel extends JPanel {
        private final Color bgColor;

        public RoundedCardPanel(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            int arc = 18;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded button used for "Generate Trial Balance" and dialog buttons
    private static class RoundedButton extends JButton {
        private final int arc = 18;

        public RoundedButton(String text) {
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
            if (getModel().isPressed()) {
                bg = bg.darker();
            } else if (getModel().isRollover()) {
                bg = bg.brighter();
            }
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}

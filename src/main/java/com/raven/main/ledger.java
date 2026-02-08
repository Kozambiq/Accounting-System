package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Ledger window.
 *
 * Left side navigation is copied from the existing design, and the center
 * content area contains:
 * - Title "Ledger"
 * - "Generate Ledger" rounded button
 * - A gray parent card that displays ledger mini-cards
 */
public class ledger extends JFrame {

    private Font workSansBold;

    private JPanel ledgerListPanel;
    private RoundedCardPanel ledgerContainerCard;
    
    // Track which accounts have already been generated to prevent duplicates
    private java.util.Set<Integer> generatedAccountIds = new java.util.HashSet<>();
    
    // Map account IDs to their ledger card components and display names for real-time updates
    private java.util.Map<Integer, RoundedCardPanel> accountLedgerCards = new java.util.HashMap<>();
    private java.util.Map<Integer, String> accountDisplayNames = new java.util.HashMap<>();
    private java.util.Map<Integer, String> accountTypes = new java.util.HashMap<>();
    /** Balance per account for Trial Balance generation (from displayed mini cards). */
    private java.util.Map<Integer, Double> accountBalances = new java.util.HashMap<>();

    /** Called when ledger cards are added or deleted so Trial Balance can refresh. */
    private Runnable onLedgerChangeCallback;

    public void setOnLedgerChange(Runnable r) {
        this.onLedgerChangeCallback = r;
    }

    public ledger() {
        setTitle("ACCOUNTING SYSTEM - Ledger");
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
        // Highlight Ledger for this screen
        menuPanel.add(createNavItem("Ledger", true, "/icon/ledger.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Trial Balance", false, "/icon/trial_balance.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Financial Reports", false, "/icon/financial_report.png"));

        side.add(logoPanel, BorderLayout.NORTH);
        side.add(menuPanel, BorderLayout.CENTER);
        side.add(windowManager.createLogoutButtonPanel(), BorderLayout.SOUTH);

        return side;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xE6E6EB));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        // Title copied from homePage, text changed to "Ledger"
        JLabel title = new JLabel("Ledger");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0x2e6417));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(title);

        stack.add(Box.createVerticalStrut(16));

        // Button panel for Generate Ledger and Refresh
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Rounded green "Generate Ledger" button
        RoundedButton generateBtn = new RoundedButton("Generate Ledger");
        generateBtn.setBackground(new Color(0x2e6417));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        generateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showGenerateLedgerDialog();
            }
        });

        // Rounded green "Refresh" button for real-time updates
        RoundedButton refreshBtn = new RoundedButton("Refresh");
        refreshBtn.setBackground(new Color(0x2e6417));
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        refreshBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                refreshAllLedgers();
            }
        });

        buttonPanel.add(generateBtn);
        buttonPanel.add(refreshBtn);
        stack.add(buttonPanel);
        stack.add(Box.createVerticalStrut(16));

        main.add(stack, BorderLayout.NORTH);

        // Full-width card container for ledger mini-cards
        ledgerContainerCard = new RoundedCardPanel(new Color(0xcdc6c6));
        ledgerContainerCard.setLayout(new BorderLayout());
        ledgerContainerCard.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        ledgerContainerCard.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Panel that tracks viewport width so FlowLayout wraps cards to the next row when they don't fit
        ledgerListPanel = new WrappingFlowPanel();
        ledgerListPanel.setOpaque(false);

        JScrollPane ledgerScrollPane = new JScrollPane(ledgerListPanel);
        ledgerScrollPane.setBorder(null);
        ledgerScrollPane.setOpaque(false);
        ledgerScrollPane.getViewport().setOpaque(false);
        ledgerScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        ledgerScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        ledgerContainerCard.add(ledgerScrollPane, BorderLayout.CENTER);
        main.add(ledgerContainerCard, BorderLayout.CENTER);

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
                // If this ledger screen is ever embedded in AppWindow, we can
                // switch cards here similarly to homePage/CoA/journalEntry.
                Window window = SwingUtilities.getWindowAncestor(item);
                if (window instanceof AppWindow appWindow) {
                    if ("DashBoard".equals(text)) {
                        appWindow.showDashboard();
                    } else if ("Chart of Accounts".equals(text)) {
                        appWindow.showChartOfAccounts();
                    } else if ("Journal Entry".equals(text)) {
                        appWindow.showJournalEntry();
                    } else if ("Ledger".equals(text)) {
                        // already here
                        appWindow.showLedger();
                    } else if ("Trial Balance".equals(text)) {
                        appWindow.showTrialBalance();
                    } else if ("Financial Reports".equals(text)) {
                        appWindow.showFinancialReports();
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

    // ----- Generate Ledger flow ---------------------------------------------

    private void showGenerateLedgerDialog() {
        JDialog dialog = new JDialog(this, "Generate Ledger", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(600, 400));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Table with same columns as CoA (without Utility)
        String[] cols = {"Date", "Time", "Account Name", "Account Type"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Load from Chart_of_Accounts
        java.util.List<Integer> accountIds = new java.util.ArrayList<>();
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT id, account_name, account_type, created_at
                       FROM Chart_of_Accounts
                      WHERE user_id = ?
                      ORDER BY created_at DESC, id DESC
                     """)) {
            Integer userId = Session.getUserId();
            if (userId != null) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int id = rs.getInt("id");
                        String accName = rs.getString("account_name");
                        String accType = rs.getString("account_type");
                        String createdAt = rs.getString("created_at");

                        String datePart = "";
                        String timePart = "";
                        if (createdAt != null) {
                            String[] parts = createdAt.split(" ");
                            if (parts.length >= 1) {
                                datePart = parts[0];
                            }
                            if (parts.length >= 2) {
                                timePart = parts[1];
                            }
                        }

                        // Convert time to 12-hour format with AM/PM
                        String displayTime = timePart;
                        try {
                            if (!timePart.isEmpty()) {
                                LocalTime t = LocalTime.parse(timePart);
                                displayTime = t.format(DateTimeFormatter.ofPattern("hh:mm a"));
                            }
                        } catch (Exception ignore) { }

                        model.addRow(new Object[]{
                                datePart,
                                displayTime,
                                ChartOfAccountsRepository.toTitleCase(accName),
                                ChartOfAccountsRepository.toTitleCase(accType)
                        });
                        accountIds.add(id);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        // Allow multiple account selection
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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

        doneBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select at least one account.",
                        "No selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Process all selected accounts
            java.util.List<Integer> newAccountIds = new java.util.ArrayList<>();
            java.util.List<String> newAccountNames = new java.util.ArrayList<>();
            
            for (int row : selectedRows) {
                if (row >= 0 && row < accountIds.size()) {
                    int accountId = accountIds.get(row);
                    // Only add if not already generated
                    if (!generatedAccountIds.contains(accountId)) {
                        newAccountIds.add(accountId);
                        newAccountNames.add(String.valueOf(model.getValueAt(row, 2)));
                        generatedAccountIds.add(accountId);
                    }
                }
            }
            
            dialog.dispose();
            
            // Generate ledgers for all newly selected accounts
            for (int i = 0; i < newAccountIds.size(); i++) {
                loadLedgerForAccount(newAccountIds.get(i), newAccountNames.get(i), false);
            }
            
            if (!newAccountIds.isEmpty()) {
                ActivityLogRepository.log("generate", "ledger", "Ledger generated");
            }
            refreshAllLedgers();
            if (onLedgerChangeCallback != null) onLedgerChangeCallback.run();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.add(root, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Load or update ledger for a specific account.
     * 
     * @param accountId The account ID
     * @param displayAccountName The display name (Title Case)
     * @param clearAll If true, clears all ledgers before loading (for single account mode)
     */
    private void loadLedgerForAccount(int accountId, String displayAccountName, boolean clearAll) {
        if (clearAll) {
            ledgerListPanel.removeAll();
            accountLedgerCards.clear();
            accountDisplayNames.clear();
            accountTypes.clear();
            accountBalances.clear();
            generatedAccountIds.clear();
        }

        // Fetch ledger lines for this account from journal_entry_lines / headers.
        // Get the account name and type from Chart_of_Accounts first
        String accountNameFromDB = null;
        String accountType = null;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT account_name, account_type FROM Chart_of_Accounts WHERE id = ?")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    accountNameFromDB = rs.getString("account_name");
                    accountType = rs.getString("account_type");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (accountNameFromDB == null || accountType == null) {
            JOptionPane.showMessageDialog(this,
                    "Account not found.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Store account type for future calculations
        accountTypes.put(accountId, accountType);

        String sql = """
                SELECT h.created_at, l.debit, l.credit
                  FROM journal_entry_headers h
                  JOIN journal_entry_lines l ON l.header_id = h.id
                 WHERE h.user_id = ? AND UPPER(l.account_name) = UPPER(?)
                 ORDER BY h.created_at, l.id
                """;

        DefaultTableModel model = new DefaultTableModel(
                new String[]{"Date", "Time", "Debit", "Credit"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Calculate balance while processing entries
        double balance = 0.0;
        String accountTypeUpper = accountType.toUpperCase();
        // Asset and Expense accounts: balance = balance + debit - credit
        // Liability, Equity, and Revenue accounts: balance = balance + credit - debit
        boolean isAssetOrExpense = accountTypeUpper.equals("ASSET") || 
                                   accountTypeUpper.equals("EXPENSE") ||
                                   accountTypeUpper.equals("EXPENSES");

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            Integer userId = Session.getUserId();
            if (userId == null) {
                JOptionPane.showMessageDialog(this,
                        "No logged-in user.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            ps.setInt(1, userId);
            ps.setString(2, accountNameFromDB);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String createdAt = rs.getString("created_at");
                    String datePart = "";
                    String timePart = "";
                    if (createdAt != null) {
                        String[] parts = createdAt.split(" ");
                        if (parts.length >= 1) {
                            datePart = parts[0];
                        }
                        if (parts.length >= 2) {
                            timePart = parts[1];
                        }
                    }
                    // Convert time to 12-hour format with AM/PM
                    String displayTime = timePart;
                    try {
                        if (!timePart.isEmpty()) {
                            LocalTime t = LocalTime.parse(timePart);
                            displayTime = t.format(DateTimeFormatter.ofPattern("hh:mm a"));
                        }
                    } catch (Exception ignore) { }

                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");

                    // Calculate balance based on account type
                    if (isAssetOrExpense) {
                        balance = balance + debit - credit;
                    } else {
                        // Liability, Equity, or Revenue
                        balance = balance + credit - debit;
                    }

                    model.addRow(new Object[]{
                            datePart,
                            displayTime,
                            debit == 0 ? "" : debit,
                            credit == 0 ? "" : credit
                    });
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Check if ledger card already exists for this account (for updates)
        RoundedCardPanel miniCard = accountLedgerCards.get(accountId);
        boolean isNewCard = (miniCard == null);
        
        // Store balance for Trial Balance generation
        accountBalances.put(accountId, balance);

        // Format balance for display
        String balanceText = formatBalance(balance);
        
        if (isNewCard) {
            // Create a new mini-card for this ledger
            miniCard = new RoundedCardPanel(Color.WHITE);
            miniCard.setLayout(new BorderLayout(0, 8));
            miniCard.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            miniCard.setPreferredSize(new Dimension(360, 180));
            
            // Create title panel with account name on left and balance on right
            JPanel titlePanel = createTitlePanel(displayAccountName, balanceText);
            miniCard.add(titlePanel, BorderLayout.NORTH);
            
            accountLedgerCards.put(accountId, miniCard);
            accountDisplayNames.put(accountId, displayAccountName);
        } else {
            // Update existing card: remove old content and re-add
            miniCard.removeAll();
            JPanel titlePanel = createTitlePanel(displayAccountName, balanceText);
            miniCard.add(titlePanel, BorderLayout.NORTH);
        }

        // Create/update the table with current data
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        miniCard.add(scroll, BorderLayout.CENTER);

        // Delete button for this ledger mini card
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonRow.setOpaque(false);
        RoundedButton deleteBtn = new RoundedButton("Delete");
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setForeground(Color.WHITE);
        final RoundedCardPanel cardForDelete = miniCard;
        deleteBtn.addActionListener(e -> deleteLedgerCard(accountId, cardForDelete));
        buttonRow.add(deleteBtn);
        miniCard.add(buttonRow, BorderLayout.SOUTH);

        if (isNewCard) {
            ledgerListPanel.add(miniCard);
        }
        
        ledgerListPanel.revalidate();
        ledgerListPanel.repaint();
    }
    
    /**
     * Delete a ledger mini card. Shows confirmation, highlights briefly, then removes.
     */
    private void deleteLedgerCard(int accountId, RoundedCardPanel miniCard) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this ledger?",
                "Delete Ledger",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;

        // Brief visual highlight before removal
        miniCard.setHighlightColor(new Color(0xFFCDD2));
        miniCard.repaint();
        Timer timer = new Timer(300, ev -> {
            ledgerListPanel.remove(miniCard);
            accountLedgerCards.remove(accountId);
            accountDisplayNames.remove(accountId);
            accountTypes.remove(accountId);
            accountBalances.remove(accountId);
            generatedAccountIds.remove(accountId);
            ActivityLogRepository.log("remove", "ledger", "Ledger card removed");
            ledgerListPanel.revalidate();
            ledgerListPanel.repaint();
            if (onLedgerChangeCallback != null) onLedgerChangeCallback.run();
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Returns ledger data for Trial Balance: list of (accountName, accountType, balance).
     * Only includes accounts that are currently displayed as mini cards.
     * Balance sign: for Asset/Expense positive = debit; for Liability/Equity/Revenue positive = credit.
     */
    public java.util.List<LedgerAccountBalance> getLedgerDataForTrialBalance() {
        java.util.List<LedgerAccountBalance> result = new java.util.ArrayList<>();
        for (java.util.Map.Entry<Integer, Double> e : accountBalances.entrySet()) {
            int accountId = e.getKey();
            double balance = e.getValue();
            String displayName = accountDisplayNames.get(accountId);
            String type = accountTypes.get(accountId);
            if (displayName != null) {
                result.add(new LedgerAccountBalance(displayName, type != null ? type : "", balance));
            }
        }
        return result;
    }

    /** Data class for Trial Balance generation from Ledger. */
    public static class LedgerAccountBalance {
        public final String accountName;
        public final String accountType;
        public final double balance;

        public LedgerAccountBalance(String accountName, String accountType, double balance) {
            this.accountName = accountName;
            this.accountType = accountType != null ? accountType : "";
            this.balance = balance;
        }
    }

    /**
     * Refresh all existing ledgers to reflect real-time database changes.
     */
    private void refreshAllLedgers() {
        if (accountLedgerCards.isEmpty()) {
            return; // No ledgers to refresh
        }
        
        for (java.util.Map.Entry<Integer, String> entry : accountDisplayNames.entrySet()) {
            int accountId = entry.getKey();
            String displayName = entry.getValue();
            loadLedgerForAccount(accountId, displayName, false);
        }
    }

    /**
     * Format balance with commas and determine if it's Debit or Credit balance.
     * 
     * @param balance The calculated balance
     * @return Formatted string like "5,000 Debit Balance" or "3,000 Credit Balance"
     */
    private String formatBalance(double balance) {
        java.text.DecimalFormat formatter = new java.text.DecimalFormat("#,##0.00");
        String formattedAmount = formatter.format(Math.abs(balance));
        
        if (balance > 0) {
            return formattedAmount + " Debit Balance";
        } else if (balance < 0) {
            return formattedAmount + " Credit Balance";
        } else {
            return "0.00 Balance";
        }
    }

    /**
     * Create a title panel with account name on the left and balance on the right.
     *
     * @param accountName The account display name (shown without any prefix)
     * @param balanceText The formatted balance text
     * @return A JPanel with BorderLayout containing the title components
     */
    private JPanel createTitlePanel(String accountName, String balanceText) {
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);

        JLabel accountLabel = new JLabel(accountName != null ? accountName : "");
        accountLabel.setFont(getWorkSansBold(14f));
        accountLabel.setForeground(new Color(0x2F2F2F));
        titlePanel.add(accountLabel, BorderLayout.WEST);

        JLabel balanceLabel = new JLabel(balanceText);
        balanceLabel.setFont(getWorkSansBold(14f));
        balanceLabel.setForeground(new Color(0x2F2F2F));
        balanceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        titlePanel.add(balanceLabel, BorderLayout.EAST);

        return titlePanel;
    }

    // ----- Shared rounded components ----------------------------------------

    // Simple rounded box used for buttons/headers
    private static class RoundedHeaderBox extends JPanel {
        private final Color bgColor;

        public RoundedHeaderBox(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
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

    /**
     * JPanel with FlowLayout that implements Scrollable so the scroll pane sizes it to the
     * viewport width. Cards wrap to the next row when they don't fit; preferred height
     * is computed for that wrapped layout so all cards are visible with vertical scroll.
     */
    private static class WrappingFlowPanel extends JPanel implements Scrollable {
        private static final int HGAP = 16;
        private static final int VGAP = 16;

        WrappingFlowPanel() {
            setLayout(new FlowLayout(FlowLayout.LEFT, HGAP, VGAP));
        }

        @Override
        public Dimension getPreferredSize() {
            int width = getWidth();
            if (width <= 0 && getParent() instanceof JViewport) {
                width = getParent().getWidth();
            }
            if (width <= 0) {
                width = 800;
            }
            int x = 0;
            int y = 0;
            int rowHeight = 0;
            for (Component c : getComponents()) {
                Dimension d = c.getPreferredSize();
                if (x + d.width + HGAP > width && x > 0) {
                    y += rowHeight + VGAP;
                    x = 0;
                    rowHeight = 0;
                }
                x += d.width + HGAP;
                rowHeight = Math.max(rowHeight, d.height);
            }
            int totalHeight = y + rowHeight;
            return new Dimension(width, totalHeight);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL ? visibleRect.height : visibleRect.width;
        }
    }

    // Rounded card panel used for parent/mini cards
    private static class RoundedCardPanel extends JPanel {
        private final Color bgColor;
        private Color highlightColor;

        public RoundedCardPanel(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }

        public void setHighlightColor(Color c) {
            this.highlightColor = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(highlightColor != null ? highlightColor : bgColor);
            int arc = 18;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded button used for "Generate Ledger" and dialog buttons
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


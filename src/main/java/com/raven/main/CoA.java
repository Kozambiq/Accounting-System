package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CoA extends JFrame {

    private Font workSansBold;
    private Font workSansRegular;
    private static final int METRIC_CARD_WIDTH = 300;
    private static final int METRIC_CARD_HEIGHT = 185;

    private DefaultTableModel accountsTableModel;
    private JTable accountsTable;
    private JLabel accountsCountLabel;
    private final List<Integer> accountIds = new ArrayList<>();
    /** Account IDs that have posted transactions (in journal_entry_lines); editing is locked for these. */
    private final Set<Integer> postedAccountIds = new HashSet<>();

    public CoA() {
        setTitle("ACCOUNTING SYSTEM - Chart of Accounts");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        setContentPane(createRootPanel());
    }

    /**
     * Build the Chart of Accounts view panel. This can be used as this frame's
     * content pane or embedded as a card in a higher-level window manager.
     */
    public JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xE6E6EB));

        JPanel sideNav = createSideNav();
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(new Color(0xE6E6EB));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top bar with title (same position as dashboard: topBar NORTH, title WEST)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel coaTitle = new JLabel("Chart of Accounts");
        coaTitle.setFont(getWorkSansBold(26f));
        coaTitle.setForeground(new Color(0x2e6417));
        topBar.add(coaTitle, BorderLayout.WEST);

        // Row below: metric card + "Add an Account" button
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        topRow.setOpaque(false);

        // Metric card: copy of homePage metric card (without badge)
        JPanel accountsMetric = createMetricCard("0", "No. of Acccounts", "/icon/chart_of_accounts_icon.png");
        accountsMetric.setPreferredSize(new Dimension(METRIC_CARD_WIDTH, METRIC_CARD_HEIGHT));
        topRow.add(accountsMetric);

        // "Add an Account" button to the right of the card
        RoundedButton addAccountButton = new RoundedButton("Add an Account");
        addAccountButton.setBackground(new Color(0x19A64A)); // same green accent used elsewhere
        addAccountButton.setForeground(Color.WHITE);
        addAccountButton.setFont(getWorkSansBold(16f));
        addAccountButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addAccountButton.setFocusPainted(false);
        addAccountButton.setBorderPainted(false);
        addAccountButton.setContentAreaFilled(false);

        try {
            ImageIcon plusIcon = new ImageIcon(getClass().getResource("/icon/plus_icon.png"));
            if (plusIcon.getIconWidth() > 0) {
                Image img = plusIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                addAccountButton.setIcon(new ImageIcon(img));
                addAccountButton.setHorizontalTextPosition(SwingConstants.RIGHT);
                addAccountButton.setIconTextGap(8);
            }
        } catch (Exception ignored) { }

        addAccountButton.addActionListener(e -> {
            AddAccountDialog dialog = new AddAccountDialog(this);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        topRow.add(addAccountButton);

        JPanel centerContent = new JPanel(new BorderLayout(20, 20));
        centerContent.setOpaque(false);
        centerContent.add(topRow, BorderLayout.NORTH);
        centerContent.add(createAccountsTablePanel(), BorderLayout.CENTER);

        mainContent.add(topBar, BorderLayout.NORTH);
        mainContent.add(centerContent, BorderLayout.CENTER);

        root.add(sideNav, BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);

        return root;
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
        menuPanel.add(createNavItem("Chart of Accounts", true, "/icon/chart_of_accounts_icon.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Journal Entry", false, "/icon/journal_entry.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Ledger", false, "/icon/ledger.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Trial Balance", false, "/icon/trial_balance.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Financial Reports", false, "/icon/financial_report.png"));

        side.add(logoPanel, BorderLayout.NORTH);
        side.add(menuPanel, BorderLayout.CENTER);
        side.add(windowManager.createLogoutButtonPanel(), BorderLayout.SOUTH);

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
                // Use the window that owns this nav item (AppWindow card),
                // not the standalone CoA frame.
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

    // Rounded container + styled JTable for the Chart of Accounts
    private JPanel createAccountsTablePanel() {
        // Outer rounded container with requested background color (#cdc6c6)
        JPanel roundedContainer = new RoundedProfileCard(new Color(0xcdc6c6));
        roundedContainer.setLayout(new BorderLayout());
        roundedContainer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Columns
        String[] columns = { "Date", "Time", "Account Name", "Account Type", "Utility" };
        accountsTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only the Utilities column (Delete button) is editable/clickable
                return column == 4;
            }
        };

        accountsTable = StandardTableStyle.createStandardTable(accountsTableModel);
        StandardTableStyle.applyStandardTableStyle(accountsTable);
        accountsTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        accountsTable.getColumnModel().getColumn(4).setCellRenderer(new UtilityButtonRenderer());
        accountsTable.getColumnModel().getColumn(4).setCellEditor(new UtilityButtonEditor());

        JScrollPane scroll = new JScrollPane(accountsTable);
        StandardTableStyle.styleScrollPaneForTable(scroll);

        roundedContainer.add(scroll, BorderLayout.CENTER);

        // Initial data load for the current user
        reloadAccountsTable();

        return roundedContainer;
    }

    // Renderer for Edit + Delete buttons in the Utility column (centered in cell)
    private class UtilityButtonRenderer implements TableCellRenderer {
        private final JPanel wrapper;

        public UtilityButtonRenderer() {
            RoundedButton editBtn = new RoundedButton("Edit");
            editBtn.setBackground(new Color(0x2e6417));
            editBtn.setForeground(Color.WHITE);
            editBtn.setFont(getWorkSansRegular(12f));
            editBtn.setFocusPainted(false);
            editBtn.setBorderPainted(false);
            editBtn.setContentAreaFilled(false);
            editBtn.setPreferredSize(new Dimension(68, 28));
            editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            RoundedButton deleteBtn = new RoundedButton("Delete");
            deleteBtn.setBackground(Color.RED);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFont(getWorkSansRegular(12f));
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false);
            deleteBtn.setPreferredSize(new Dimension(82, 28));
            deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            inner.setOpaque(false);
            inner.add(editBtn);
            inner.add(deleteBtn);
            wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(inner, new GridBagConstraints());
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return wrapper;
        }
    }

    // Editor for Edit + Delete buttons in the Utility column (centered in cell; Edit locked when posted)
    private class UtilityButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final RoundedButton editButton;
        private final RoundedButton deleteButton;
        private final JPanel wrapper;
        private int currentRow = -1;

        public UtilityButtonEditor() {
            editButton = new RoundedButton("Edit");
            editButton.setBackground(new Color(0x2e6417));
            editButton.setForeground(Color.WHITE);
            editButton.setFont(getWorkSansRegular(12f));
            editButton.setOpaque(false);
            editButton.setFocusPainted(false);
            editButton.setBorderPainted(false);
            editButton.setContentAreaFilled(false);
            editButton.setPreferredSize(new Dimension(68, 28));
            editButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            editButton.addActionListener(e -> {
                if (currentRow >= 0 && currentRow < accountIds.size()) {
                    if (postedAccountIds.contains(accountIds.get(currentRow))) {
                        showCannotEditPostedDialog();
                    } else {
                        handleEditAtRow(currentRow);
                    }
                }
                fireEditingStopped();
            });

            deleteButton = new RoundedButton("Delete");
            deleteButton.setBackground(Color.RED);
            deleteButton.setForeground(Color.WHITE);
            deleteButton.setFont(getWorkSansRegular(12f));
            deleteButton.setOpaque(false);
            deleteButton.setFocusPainted(false);
            deleteButton.setBorderPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setPreferredSize(new Dimension(82, 28));
            deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            deleteButton.addActionListener(e -> {
                if (currentRow >= 0 && currentRow < accountIds.size()) handleDeleteAtRow(currentRow);
                fireEditingStopped();
            });

            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            inner.setOpaque(false);
            inner.add(editButton);
            inner.add(deleteButton);
            wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(inner, new GridBagConstraints());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            boolean posted = row >= 0 && row < accountIds.size() && postedAccountIds.contains(accountIds.get(row));
            // Keep button clickable so the action listener runs and shows the "cannot edit" dialog
            editButton.setEnabled(true);
            editButton.setToolTipText(posted ? "Editing disabled — account has posted transactions" : null);
            editButton.setBackground(posted ? new Color(0x8a9a7a) : new Color(0x2e6417));
            return wrapper;
        }

        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    private void showCannotEditPostedDialog() {
        JDialog d = new JDialog(this, "Edit Not Allowed", true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setLayout(new BorderLayout(12, 12));
        JLabel msg = new JLabel("<html><div style='text-align:center;'>This account cannot be edited because it already has posted transactions.</div></html>");
        msg.setFont(getWorkSansRegular(14f));
        msg.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        d.add(msg, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        RoundedButton okBtn = new RoundedButton("OK");
        okBtn.setBackground(new Color(0x2e6417));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(getWorkSansRegular(14f));
        okBtn.addActionListener(e -> d.dispose());
        btnPanel.add(okBtn);
        d.add(btnPanel, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private void handleEditAtRow(int row) {
        if (row < 0 || row >= accountIds.size()) return;
        if (postedAccountIds.contains(accountIds.get(row))) {
            showCannotEditPostedDialog();
            return;
        }
        int accountId = accountIds.get(row);
        String currentName = (String) accountsTableModel.getValueAt(row, 2);
        String currentType = (String) accountsTableModel.getValueAt(row, 3);

        Integer userId = Session.getUserId();
        if (userId == null) return;
        String storedName = null;
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("SELECT account_name FROM Chart_of_Accounts WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, accountId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) storedName = rs.getString("account_name");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
        final String oldNormalized = storedName != null ? storedName.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ") : "";

        JDialog dialog = new JDialog(this, "Edit Account", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel nameLabel = new JLabel("Account Name:");
        nameLabel.setFont(getWorkSansRegular(14f));
        form.add(nameLabel);
        form.add(Box.createVerticalStrut(4));
        JTextField nameField = new JTextField(currentName != null ? currentName : "", 24);
        nameField.setFont(getWorkSansRegular(14f));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x999999), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        form.add(nameField);
        form.add(Box.createVerticalStrut(12));
        JLabel typeLabel = new JLabel("Account Type:");
        typeLabel.setFont(getWorkSansRegular(14f));
        form.add(typeLabel);
        form.add(Box.createVerticalStrut(4));
        String[] types = { "Asset", "Liability", "Equity", "Revenues", "Expenses" };
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(getWorkSansRegular(14f));
        if (currentType != null) {
            for (int i = 0; i < types.length; i++) {
                if (currentType.equalsIgnoreCase(types[i])) { typeCombo.setSelectedIndex(i); break; }
            }
        }
        form.add(typeCombo);
        dialog.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        RoundedButton saveBtn = new RoundedButton("Save");
        saveBtn.setBackground(new Color(0x2e6417));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(getWorkSansRegular(14f));
        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(getWorkSansRegular(14f));
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText() != null ? nameField.getText().trim() : "";
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Account name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String newType = (String) typeCombo.getSelectedItem();
            if (newType == null) return;
            String normalized = newName.toUpperCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
            String updateSql = "UPDATE Chart_of_Accounts SET account_name = ?, account_type = ? WHERE id = ? AND user_id = ?";
            Integer uid = Session.getUserId();
            if (uid == null) return;
            try (Connection conn = DBConnection.connect();
                 PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setString(1, normalized);
                ps.setString(2, newType);
                ps.setInt(3, accountId);
                ps.setInt(4, uid);
                ps.executeUpdate();
                JournalEntryRepository.updateAccountNameInLines(uid, oldNormalized, normalized);
                ActivityLogRepository.log("edit", "chart_of_accounts", "Account " + ChartOfAccountsRepository.toTitleCase(normalized) + " updated");
                NotificationRepository.insert(uid, "Account edited: " + ChartOfAccountsRepository.toTitleCase(normalized));
                reloadAccountsTable();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Failed to update account.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dialog.dispose();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttons.add(saveBtn);
        buttons.add(cancelBtn);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Load Chart_of_Accounts rows for the currently logged-in user and
     * populate the JTable model. Account names and types are rendered in
     * Title Case, while date and time are split from the stored timestamp.
     */
    private void reloadAccountsTable() {
        if (accountsTableModel == null) {
            return;
        }

        accountsTableModel.setRowCount(0);

        Integer userId = Session.getUserId();
        if (userId == null) {
            return;
        }

        String sql = """
                SELECT id, account_name, account_type, created_at
                  FROM Chart_of_Accounts
                 WHERE user_id = ?
                 ORDER BY created_at DESC, id DESC
                """;

        Set<String> postedNames = JournalEntryRepository.getPostedAccountNamesNormalized(userId);

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            accountIds.clear();
            postedAccountIds.clear();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int accountId = rs.getInt("id");
                    String storedName = rs.getString("account_name");
                    String storedType = rs.getString("account_type");
                    String createdAt = rs.getString("created_at");
                    String normalized = storedName != null ? storedName.trim().toUpperCase(Locale.ROOT) : "";
                    if (postedNames.contains(normalized)) {
                        postedAccountIds.add(accountId);
                    }

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

                    // Convert to 12-hour format with AM/PM
                    String displayTime = timePart;
                    try {
                        if (!timePart.isEmpty()) {
                            LocalTime t = LocalTime.parse(timePart);
                            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a");
                            displayTime = t.format(fmt);
                        }
                    } catch (Exception ignore) {
                        // Fallback to original if parsing fails
                    }

                    String displayName = ChartOfAccountsRepository.toTitleCase(storedName);
                    String displayType = ChartOfAccountsRepository.toTitleCase(storedType);

                    accountIds.add(accountId);

                    accountsTableModel.addRow(new Object[]{
                            datePart,
                            displayTime,
                            displayName,
                            displayType,
                            "Delete"
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (accountsCountLabel != null) {
                accountsCountLabel.setText(String.valueOf(accountsTableModel.getRowCount()));
            }
        }
    }

    private void handleDeleteAtRow(int row) {
        int accountId = accountIds.get(row);
        String accountName = (String) accountsTableModel.getValueAt(row, 2);

        String message = "Are you sure you want to delete Account " + accountName;

        JDialog dialog = new JDialog(this, "Confirm Delete", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        JLabel label = new JLabel(message);
        label.setFont(getWorkSansRegular(14f));
        label.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        dialog.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.setBackground(new Color(0x19A64A));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(getWorkSansRegular(14f));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(getWorkSansRegular(14f));

        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        confirmBtn.addActionListener(e -> {
            Integer userId = Session.getUserId();
            if (userId != null) {
                String deleteSql = "DELETE FROM Chart_of_Accounts WHERE id = ? AND user_id = ?";
                try (Connection conn = DBConnection.connect();
                     PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                    ps.setInt(1, accountId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                    ActivityLogRepository.log("remove", "chart_of_accounts", "Chart of Accounts account removed");
                    NotificationRepository.insert(userId, "Account deleted: " + accountName);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                reloadAccountsTable();
            }
            dialog.dispose();
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    // Copy of the dashboard metric card (without the badge), for \"No. of Acccounts\"
    private JPanel createMetricCard(String amount, String label, String iconPath) {
        JPanel card = new RoundedProfileCard(new Color(0xcdc6c6));
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Icon at top left (optional)
        JPanel iconPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        iconPanel.setOpaque(false);
        iconPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                if (icon.getIconWidth() > 0) {
                    Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                    iconPanel.add(new JLabel(new ImageIcon(img)));
                }
            } catch (Exception ignored) { }
        }
        card.add(iconPanel, BorderLayout.NORTH);

        // Amount + label section at the bottom (same layout as homePage, without badge)
        JPanel bottomContent = new JPanel();
        bottomContent.setLayout(new BoxLayout(bottomContent, BoxLayout.Y_AXIS));
        bottomContent.setOpaque(false);

        bottomContent.add(Box.createVerticalGlue());

        accountsCountLabel = new JLabel(amount);
        accountsCountLabel.setFont(getWorkSansBold(34f));
        accountsCountLabel.setForeground(new Color(0x2F2F2F));
        accountsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(getWorkSansRegular(27f));
        descLabel.setForeground(new Color(0x555555));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomContent.add(accountsCountLabel);
        bottomContent.add(descLabel);

        card.add(bottomContent, BorderLayout.CENTER);

        return card;
    }

    private Font getWorkSansBold(float size) {
        if (workSansBold != null) {
            return workSansBold.deriveFont(Font.BOLD, size);
        }
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    private Font getWorkSansRegular(float size) {
        if (workSansRegular != null) {
            return workSansRegular.deriveFont(Font.PLAIN, size);
        }
        try (InputStream stream = getClass().getResourceAsStream("/fonts/Work_Sans/static/WorkSans-Regular.ttf")) {
            if (stream != null) {
                workSansRegular = Font.createFont(Font.TRUETYPE_FONT, stream);
                return workSansRegular.deriveFont(size);
            }
        } catch (Exception ignored) { }
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }

    // --- Add Account Dialog -------------------------------------------------

    /**
     * Modal dialog used to add Chart of Accounts rows.
     * Uses the same rounded visual theme and Work Sans fonts.
     */
    private class AddAccountDialog extends JDialog {
        private final Font workSansRegular;
        private final Font workSansBoldLocal;

        private JTextField accountNameField;
        private JComboBox<String> accountTypeCombo;
        private JLabel errorLabel;

        private final ChartOfAccountsRepository repository = new ChartOfAccountsRepository();

        public AddAccountDialog(Frame owner) {
            super(owner, "Add an Account", true);

            // Load Work Sans fonts specifically for this dialog
            this.workSansRegular = loadFont("/fonts/Work_Sans/static/WorkSans-Regular.ttf", 14f);
            this.workSansBoldLocal = loadFont("/fonts/Work_Sans/static/WorkSans-Bold.ttf", 14f);

            initDialogUI();
        }

        private void initDialogUI() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(0xE6E6EB));

            RoundedProfileCard card = new RoundedProfileCard(Color.WHITE);
            card.setLayout(new BorderLayout(0, 16));
            card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

            JPanel formPanel = new JPanel();
            formPanel.setOpaque(false);
            formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

            // Account Name label
            JLabel nameLabel = new JLabel("Account Name:");
            nameLabel.setFont(workSansBoldLocal.deriveFont(14f));
            formPanel.add(nameLabel);
            formPanel.add(Box.createVerticalStrut(6));

            // Rounded text field for account name
            accountNameField = new JTextField();
            accountNameField.setFont(workSansRegular.deriveFont(14f));

            RoundedInputWrapper nameWrapper = new RoundedInputWrapper(accountNameField);
            accountNameField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x999999), 1),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
            formPanel.add(nameWrapper);
            formPanel.add(Box.createVerticalStrut(14));

            // Account Type label
            JLabel typeLabel = new JLabel("Select Account type");
            typeLabel.setFont(workSansRegular.deriveFont(14f));
            formPanel.add(typeLabel);
            formPanel.add(Box.createVerticalStrut(6));

            // Rounded combo box for account type
            String[] accountTypes = {
                    "Asset",
                    "Liability",
                    "Equity",
                    "Revenues",
                    "Expenses"
            };
            accountTypeCombo = new JComboBox<>(accountTypes);
            accountTypeCombo.setFont(workSansRegular.deriveFont(14f));
            accountTypeCombo.setFocusable(false);

            RoundedInputWrapper typeWrapper = new RoundedInputWrapper(accountTypeCombo);
            formPanel.add(typeWrapper);
            formPanel.add(Box.createVerticalStrut(10));

            // Error label
            errorLabel = new JLabel(" ");
            errorLabel.setFont(workSansRegular.deriveFont(12f));
            errorLabel.setForeground(new Color(0xCC0000));
            formPanel.add(errorLabel);

            card.add(formPanel, BorderLayout.CENTER);

            // Buttons at the bottom, aligned to the right
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            buttonPanel.setOpaque(false);

            RoundedButton saveAndDoneBtn = new RoundedButton("Save and Done");
            saveAndDoneBtn.setBackground(new Color(0x19A64A));
            saveAndDoneBtn.setForeground(Color.WHITE);
            saveAndDoneBtn.setFont(workSansRegular.deriveFont(14f));

            RoundedButton saveAndMoreBtn = new RoundedButton("Save and Add More");
            saveAndMoreBtn.setBackground(new Color(0x19A64A));
            saveAndMoreBtn.setForeground(Color.WHITE);
            saveAndMoreBtn.setFont(workSansRegular.deriveFont(14f));

            RoundedButton cancelBtn = new RoundedButton("Cancel");
            cancelBtn.setBackground(Color.RED);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFont(workSansRegular.deriveFont(14f));

            saveAndDoneBtn.addActionListener(e -> {
                if (validateAndSave(true)) {
                    dispose();
                }
            });

            saveAndMoreBtn.addActionListener(e -> validateAndSave(false));

            cancelBtn.addActionListener(e -> dispose());

            buttonPanel.add(saveAndDoneBtn);
            buttonPanel.add(saveAndMoreBtn);
            buttonPanel.add(cancelBtn);

            card.add(buttonPanel, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            setContentPane(root);

            pack();
            setResizable(false);
        }

        private boolean validateAndSave(boolean closeAfterSave) {
            String rawName = accountNameField.getText() != null
                    ? accountNameField.getText().trim()
                    : "";

            if (rawName.isEmpty()) {
                showValidationError("Account Name is required.");
                return false;
            }

            // Allow only letters (including accented) and spaces
            if (!rawName.matches("[\\p{L} ]+")) {
                showValidationError("Account Name can only contain letters and spaces.");
                return false;
            }

            String normalizedName = normalizeAccountName(rawName);
            String accountType = (String) accountTypeCombo.getSelectedItem();
            if (accountType == null || accountType.isBlank()) {
                showValidationError("Please select an account type.");
                return false;
            }

            try {
                if (repository.accountNameExistsForCurrentUser(normalizedName)) {
                    showValidationError("This account name already exists for your profile.");
                    return false;
                }

                repository.insertAccountForCurrentUser(normalizedName, accountType);
                ActivityLogRepository.log("add", "chart_of_accounts", "Account " + ChartOfAccountsRepository.toTitleCase(normalizedName) + " added");
                reloadAccountsTable();

            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "No logged-in user was found. Please log in again.",
                        "Session Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "An error occurred while saving the account. Please try again.",
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;
            }

            // Saved successfully
            if (closeAfterSave) {
                return true;
            } else {
                accountNameField.setText("");
                accountTypeCombo.setSelectedIndex(0);
                errorLabel.setText(" ");
                accountNameField.requestFocusInWindow();
                return true;
            }
        }

        private void showValidationError(String message) {
            errorLabel.setText(message);
        }

        private String normalizeAccountName(String input) {
            String trimmed = input.trim().replaceAll("\\s+", " ");
            return trimmed.toUpperCase(Locale.ROOT);
        }

        private Font loadFont(String path, float size) {
            try (InputStream stream = getClass().getResourceAsStream(path)) {
                if (stream == null) {
                    return new Font("SansSerif", Font.PLAIN, (int) size);
                }
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                return font.deriveFont(size);
            } catch (Exception e) {
                e.printStackTrace();
                return new Font("SansSerif", Font.PLAIN, (int) size);
            }
        }
    }

    /**
     * Small rounded wrapper panel used to give inputs a rounded-corner
     * background while keeping the inner Swing components simple.
     */
    private static class RoundedInputWrapper extends JPanel {
        private final int arc = 18;

        public RoundedInputWrapper(JComponent inner) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            inner.setOpaque(false);
            if (inner instanceof JTextField) {
                ((JTextField) inner).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            } else if (inner instanceof JComboBox) {
                ((JComboBox<?>) inner).setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            }

            add(inner, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.setColor(new Color(0xDDDDDD));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }


    // Rounded background for side-nav items
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

    // Rounded button used for \"Add an Account\"
    private static class RoundedButton extends JButton {
        private int arc = 20;

        public RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
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

            // Let the default button paint its text and icon over the custom background
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Rounded card panel, same style as dashboard metric/profile cards
    private static class RoundedProfileCard extends JPanel {
        private final Color bgColor;

        public RoundedProfileCard(Color bgColor) {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CoA frame = new CoA();
            frame.setVisible(true);
        });
    }
}

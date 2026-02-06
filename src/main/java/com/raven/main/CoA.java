package com.raven.main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.List;
import java.util.Locale;

public class CoA extends JFrame {

    private Font workSansBold;
    private Font workSansRegular;
    private static final int METRIC_CARD_WIDTH = 300;
    private static final int METRIC_CARD_HEIGHT = 185;

    private DefaultTableModel accountsTableModel;
    private JTable accountsTable;
    private JLabel accountsCountLabel;
    private final List<Integer> accountIds = new ArrayList<>();

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

        // Top row: metric card + "Add an Account" button
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
        addAccountButton.setFont(new Font("SansSerif", Font.BOLD, 16));
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

        // Put top row at the top, table fills remaining space
        mainContent.add(topRow, BorderLayout.NORTH);
        mainContent.add(createAccountsTablePanel(), BorderLayout.CENTER);

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

        accountsTable = new RoundedRowTable(accountsTableModel);
        accountsTable.setFillsViewportHeight(true);
        // slightly taller rows for better readability
        accountsTable.setRowHeight(40);
        accountsTable.setShowHorizontalLines(false);
        accountsTable.setShowVerticalLines(false);
        accountsTable.setIntercellSpacing(new Dimension(0, 0));
        // Remove hover/selection highlight effect by disabling selection
        accountsTable.setRowSelectionAllowed(false);
        accountsTable.setColumnSelectionAllowed(false);
        accountsTable.setCellSelectionEnabled(false);
        accountsTable.setFocusable(false);
        Color selectionColor = new Color(0x34C96D);              // kept for renderer signature
        // Background for empty area (no rows) matches card background
        Color tableBg = new Color(0xcdc6c6);
        accountsTable.setBackground(tableBg);
        accountsTable.setOpaque(true);
        accountsTable.setGridColor(new Color(0xE0E0E0));
        accountsTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Alternating row colors & rounded rows handled in custom table/renderer
        accountsTable.setDefaultRenderer(Object.class, new AlternatingRowRenderer(selectionColor));
        // Utilities column uses a Delete button
        accountsTable.getColumnModel().getColumn(4).setCellRenderer(new DeleteButtonRenderer());
        accountsTable.getColumnModel().getColumn(4).setCellEditor(new DeleteButtonEditor());

        // Header styling
        JTableHeader header = accountsTable.getTableHeader();
        header.setBackground(new Color(0x19A64A));
        header.setForeground(Color.WHITE);
        header.setFont(getWorkSansBold(14f));
        header.setReorderingAllowed(false);

        Dimension headerSize = header.getPreferredSize();
        headerSize.height = headerSize.height + 8;       // a bit taller than default
        header.setPreferredSize(headerSize);

        JScrollPane scroll = new JScrollPane(accountsTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        // Ensure the viewport has a solid background so no underlying images show through
        scroll.getViewport().setOpaque(true);
        scroll.getViewport().setBackground(new Color(0xcdc6c6));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        roundedContainer.add(scroll, BorderLayout.CENTER);

        // Initial data load for the current user
        reloadAccountsTable();

        return roundedContainer;
    }

    /**
     * Custom JTable that paints each entire row as a single rounded rectangle
     * spanning all columns. Alternating row colors and selection are handled
     * here so that rows appear continuous instead of per-cell pills.
     */
    private static class RoundedRowTable extends JTable {
        private final Color evenRowColor = Color.WHITE;
        private final Color oddRowColor  = new Color(0xF2, 0xF2, 0xF2);
        private final Color selectionRowColor = new Color(0x34, 0xC9, 0x6D); // lighter shade of 0x19A64A

        public RoundedRowTable(TableModel model) {
            super(model);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Base background for the whole table area to prevent any underlying
            // icons or images from showing through.
            g2.setColor(new Color(0xcdc6c6));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int rowCount = getRowCount();
            int rowHeight = getRowHeight();
            int width = getWidth();
            int arc = 18;
            int marginX = 4;   // small horizontal margin inside the card
            int marginY = 4;   // larger vertical inset for more space between rows

            // Paint rounded background for each visible row
            for (int row = 0; row < rowCount; row++) {
                int y = row * rowHeight;

                // We intentionally ignore selection here to remove click effects.
                Color bg = (row % 2 == 0) ? evenRowColor : oddRowColor;

                g2.setColor(bg);
                g2.fillRoundRect(
                        marginX,
                        y + marginY,
                        width - marginX * 2,
                        rowHeight - marginY * 2,
                        arc,
                        arc
                );
            }

            g2.dispose();

            // Let JTable paint text and grid lines, but skip its own background fill
            boolean wasOpaque = isOpaque();
            setOpaque(false);
            super.paintComponent(g);
            setOpaque(wasOpaque);
        }
    }

    // Alternating row renderer that relies on the table's row painting
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private final Color textColor = new Color(0x2F, 0x2F, 0x2F);
        private final Color selectionBg;
        private final Color selectionFg = Color.WHITE;

        public AlternatingRowRenderer(Color selectionBg) {
            this.selectionBg = selectionBg;
            setOpaque(false);
            // Extra left padding so text does not clash with rounded corners
            // (slightly more to the right per column as requested)
            setBorder(new EmptyBorder(6, 40, 6, 10));
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, false, hasFocus, row, column);

            // Use dark text on light backgrounds; keep white text when selected
            setForeground(isSelected ? selectionFg : textColor);
            setHorizontalAlignment(LEFT);

            return this;
        }
    }

    // Renderer for the "Delete" button in the Utilities column
    private class DeleteButtonRenderer implements TableCellRenderer {
        private final RoundedButton button;
        private final JPanel wrapper;

        public DeleteButtonRenderer() {
            button = new RoundedButton("Delete");
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setFont(getWorkSansRegular(12f));
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setPreferredSize(new Dimension(70, 24));

            wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(button);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return wrapper;
        }
    }

    // Editor for the "Delete" button in the Utilities column
    private class DeleteButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final RoundedButton button;
        private final JPanel wrapper;
        private int currentRow = -1;

        public DeleteButtonEditor() {
            button = new RoundedButton("Delete");
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.setFont(getWorkSansRegular(12f));
            button.setOpaque(false);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setPreferredSize(new Dimension(70, 24));
            button.addActionListener(this);

            wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(button);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            return wrapper;
        }

        @Override
        public Object getCellEditorValue() {
            return "Delete";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentRow >= 0 && currentRow < accountIds.size()) {
                handleDeleteAtRow(currentRow);
            }
            fireEditingStopped();
        }
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

        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            accountIds.clear();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int accountId = rs.getInt("id");
                    String storedName = rs.getString("account_name");
                    String storedType = rs.getString("account_type");
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
        accountsCountLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        accountsCountLabel.setForeground(new Color(0x2F2F2F));
        accountsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 27));
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
            accountNameField.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

            RoundedInputWrapper nameWrapper = new RoundedInputWrapper(accountNameField);
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
                // Refresh the main table so the new entry appears immediately
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

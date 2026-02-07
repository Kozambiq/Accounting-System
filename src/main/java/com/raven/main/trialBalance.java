package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
    private final ledger ledgerFrame;
    private JLabel errorLabel;
    /** True after user has generated at least once; used to auto-refresh when Ledger changes. */
    private boolean trialBalanceGenerated;

    public trialBalance(ledger ledgerFrame) {
        this.ledgerFrame = ledgerFrame;
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

        // Error message (shown when no ledger cards)
        errorLabel = new JLabel();
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(errorLabel);
        stack.add(Box.createVerticalStrut(8));

        // Rounded green "Generate Trial Balance" button
        RoundedButton generateBtn = new RoundedButton("Generate Trial Balance");
        generateBtn.setBackground(new Color(0x2e6417));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        generateBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        generateBtn.addActionListener(e -> generateTrialBalanceFromLedger());

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

    // ----- Generate Trial Balance flow ---------------------------------------------

    /**
     * Refresh Trial Balance table from current Ledger data. Called when user clicks
     * Generate Trial Balance or when Ledger mini cards are added/deleted (only if table was already generated).
     */
    public void refreshTrialBalanceFromLedger() {
        if (!trialBalanceGenerated) return;
        generateTrialBalanceFromLedger();
    }

    /**
     * Generate Trial Balance from Ledger mini cards only. No modal.
     * Displays inline error if no ledger cards exist.
     */
    private void generateTrialBalanceFromLedger() {
        java.util.List<ledger.LedgerAccountBalance> ledgerData = ledgerFrame.getLedgerDataForTrialBalance();

        if (ledgerData.isEmpty()) {
            errorLabel.setText("No ledger records found. Please generate ledger entries before creating a trial balance.");
            errorLabel.setVisible(true);
            trialBalanceContainer.removeAll();
            trialBalanceContainer.revalidate();
            trialBalanceContainer.repaint();
            trialBalanceGenerated = false;
            return;
        }

        trialBalanceGenerated = true;
        errorLabel.setText("");
        errorLabel.setVisible(false);
        displayTrialBalanceFromLedger(ledgerData);
    }

    private void displayTrialBalanceFromLedger(java.util.List<ledger.LedgerAccountBalance> ledgerData) {
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

        for (ledger.LedgerAccountBalance item : ledgerData) {
            // Ledger: Asset/Expense → positive balance = debit; Liability/Equity/Revenue → positive balance = credit
            double debit = 0.0;
            double credit = 0.0;
            String typeUpper = (item.accountType != null ? item.accountType : "").toUpperCase();
            boolean isAssetOrExpense = typeUpper.equals("ASSET") || typeUpper.equals("EXPENSE") || typeUpper.equals("EXPENSES");

            if (item.balance > 0) {
                if (isAssetOrExpense) {
                    debit = item.balance;
                    totalDebit += debit;
                } else {
                    credit = item.balance;
                    totalCredit += credit;
                }
            } else if (item.balance < 0) {
                if (isAssetOrExpense) {
                    credit = Math.abs(item.balance);
                    totalCredit += credit;
                } else {
                    debit = Math.abs(item.balance);
                    totalDebit += debit;
                }
            }
            // balance == 0: leave both columns blank

            model.addRow(new Object[]{
                    ChartOfAccountsRepository.toTitleCase(item.accountName),
                    debit == 0 ? "" : formatNumber(debit),
                    credit == 0 ? "" : formatNumber(credit)
            });
        }

        // Totals row (positive values only)
        model.addRow(new Object[]{"TOTAL", formatNumber(totalDebit), formatNumber(totalCredit)});

        trialBalanceTable = new JTable(model);
        trialBalanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        trialBalanceTable.setFillsViewportHeight(true);

        final boolean totalsMismatch = Math.abs(totalDebit - totalCredit) > 0.01;

        // Right-align Debit and Credit columns; optionally highlight totals row when unbalanced
        TableCellRenderer rightAlignRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                          boolean isSelected, boolean hasFocus,
                                                          int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (totalsMismatch && row == table.getRowCount() - 1) {
                    c.setBackground(Color.RED);
                    c.setForeground(Color.WHITE);
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                }
                return c;
            }
        };
        trialBalanceTable.getColumnModel().getColumn(1).setCellRenderer(rightAlignRenderer);
        trialBalanceTable.getColumnModel().getColumn(2).setCellRenderer(rightAlignRenderer);

        // Account Name column: left-align; highlight totals row when unbalanced
        if (totalsMismatch) {
            trialBalanceTable.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                                                              boolean isSelected, boolean hasFocus,
                                                              int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (row == table.getRowCount() - 1) {
                        c.setBackground(Color.RED);
                        c.setForeground(Color.WHITE);
                    }
                    return c;
                }
            });
        }

        JScrollPane scroll = new JScrollPane(trialBalanceTable);
        trialBalanceContainer.add(scroll, BorderLayout.CENTER);

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

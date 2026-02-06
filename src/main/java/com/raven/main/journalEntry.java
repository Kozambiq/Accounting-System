package com.raven.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Journal Entry main window.
 *
 * Left side navigation is copied from the existing design, and the center
 * content area contains:
 * - Title "Journal Entry"
 * - "Add new entry" rounded box
 * - Search bar with a green search box
 * - A list of saved journal entry cards
 */
public class journalEntry extends JFrame {

    private Font workSansBold;
    private final JournalEntryRepository journalRepo = new JournalEntryRepository();

    // In-memory sheet rows for the current journal entry being edited
    private java.util.List<JournalEntryRepository.JournalLine> currentSheetLines =
            new java.util.ArrayList<>();

    private JPanel entriesListPanel;

    public journalEntry() {
        setTitle("ACCOUNTING SYSTEM - Journal Entry");
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
        // Highlight Journal Entry for this screen
        menuPanel.add(createNavItem("Journal Entry", true, "/icon/journal_entry.png"));
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

    /**
     * Center content for the Journal Entry screen.
     *
     * Layout:
     * - Title "Journal Entry" (same style as homePage title)
     * - Rounded green "Add new entry" box
     * - Search bar row with input + green search box
     * - List of saved journal entry cards
     */
    private JPanel createMainContent() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xE6E6EB));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        // Title copied from homePage, text changed to "Journal Entry"
        JLabel title = new JLabel("Journal Entry");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0x2e6417));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        stack.add(title);

        stack.add(Box.createVerticalStrut(16));

        // Rounded green "Add new entry" box (slightly smaller)
        RoundedHeaderBox addEntryBox = new RoundedHeaderBox(new Color(0x2e6417));
        addEntryBox.setLayout(new GridBagLayout());
        addEntryBox.setMaximumSize(new Dimension(200, 36));
        addEntryBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        addEntryBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel addEntryLabel = new JLabel("Add new entry");
        addEntryLabel.setForeground(Color.WHITE);
        addEntryLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        addEntryBox.add(addEntryLabel);

        // Start the multi-step journal entry flow when clicked
        addEntryBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                startNewJournalEntry();
            }
        });

        stack.add(addEntryBox);
        stack.add(Box.createVerticalStrut(16));

        // Search bar row: text field + green search box on the right
        JPanel searchRow = new JPanel(new BorderLayout());
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.setMaximumSize(new Dimension(420, 40));

        JTextField searchField = new JTextField();
        // Solid 1px black outline with inner padding
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));

        RoundedHeaderBox searchButton = new RoundedHeaderBox(new Color(0x2e6417));
        searchButton.setPreferredSize(new Dimension(90, 40));
        searchButton.setLayout(new GridBagLayout());
        JLabel searchLabel = new JLabel("Search");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        searchButton.add(searchLabel);

        searchRow.add(searchField, BorderLayout.CENTER);
        searchRow.add(searchButton, BorderLayout.EAST);

        stack.add(searchRow);

        // Container for saved journal entry cards
        entriesListPanel = new JPanel();
        entriesListPanel.setOpaque(false);
        entriesListPanel.setLayout(new BoxLayout(entriesListPanel, BoxLayout.Y_AXIS));
        entriesListPanel.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        stack.add(entriesListPanel);

        main.add(stack, BorderLayout.NORTH);

        // initial load of existing journal entries
        reloadJournalCards();

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
                System.out.println("Clicked: " + text);
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

    // ----- Journal entry flow helpers ---------------------------------------

    private void startNewJournalEntry() {
        currentSheetLines = new java.util.ArrayList<>();
        showAddEntryDialog();
    }

    private void showAddEntryDialog() {
        JDialog dialog = new JDialog(this, "New Journal Entry", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Involved accounts:");
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(label);
        content.add(Box.createVerticalStrut(12));

        RoundedHeaderBox selectBtn = new RoundedHeaderBox(new Color(0x2e6417));
        selectBtn.setLayout(new GridBagLayout());
        selectBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel btnText = new JLabel("Add / Select accounts");
        btnText.setForeground(Color.WHITE);
        btnText.setFont(new Font("SansSerif", Font.BOLD, 14));
        selectBtn.add(btnText);
        selectBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        selectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                showSheetDialog();
            }
        });

        content.add(selectBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSheetDialog() {
        JDialog dialog = new JDialog(this, "Journal Entry - Sheet", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(500, 400));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Top: Select Account button
        RoundedHeaderBox selectAccountBtn = new RoundedHeaderBox(new Color(0x2e6417));
        selectAccountBtn.setLayout(new GridBagLayout());
        JLabel selectLabel = new JLabel("Select Account");
        selectLabel.setForeground(Color.WHITE);
        selectLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        selectAccountBtn.add(selectLabel);
        selectAccountBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topPanel.setOpaque(false);
        topPanel.add(selectAccountBtn);

        root.add(topPanel, BorderLayout.NORTH);

        // Center: sheet table
        String[] cols = {"Account Name", "Debit", "Credit"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        // Allow editing debit/credit like a sheet
                        return column == 1 || column == 2;
                    }
                };

        // Populate from currentSheetLines
        for (JournalEntryRepository.JournalLine line : currentSheetLines) {
            model.addRow(new Object[]{
                    line.accountName,
                    line.debit == 0 ? "" : line.debit,
                    line.credit == 0 ? "" : line.credit
            });
        }

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        // Bottom: Save / Cancel
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(new Color(0x2e6417));
        saveBtn.setForeground(Color.WHITE);

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);

        bottom.add(saveBtn);
        bottom.add(cancelBtn);

        root.add(bottom, BorderLayout.SOUTH);

        dialog.add(root, BorderLayout.CENTER);

        // Wiring
        selectAccountBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dialog.dispose();
                showSelectAccountDialog();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        saveBtn.addActionListener(e -> {
            // Sync model into currentSheetLines
            java.util.List<JournalEntryRepository.JournalLine> lines = new java.util.ArrayList<>();
            double totalDebit = 0;
            double totalCredit = 0;

            for (int r = 0; r < model.getRowCount(); r++) {
                String accountName = String.valueOf(model.getValueAt(r, 0)).trim();
                String debitStr = String.valueOf(model.getValueAt(r, 1)).trim();
                String creditStr = String.valueOf(model.getValueAt(r, 2)).trim();

                if (accountName.isEmpty()) {
                    continue;
                }

                double debit = 0;
                double credit = 0;
                if (!debitStr.isEmpty()) {
                    try {
                        debit = Double.parseDouble(debitStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Invalid debit amount on row " + (r + 1),
                                "Validation error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if (!creditStr.isEmpty()) {
                    try {
                        credit = Double.parseDouble(creditStr);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog,
                                "Invalid credit amount on row " + (r + 1),
                                "Validation error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if ((debit > 0 && credit > 0) || (debit == 0 && credit == 0)) {
                    JOptionPane.showMessageDialog(dialog,
                            "Each row must have either Debit or Credit (but not both) on row " + (r + 1),
                            "Validation error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                totalDebit += debit;
                totalCredit += credit;
                lines.add(new JournalEntryRepository.JournalLine(accountName, debit, credit));
            }

            if (lines.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Add at least one line before saving.",
                        "Validation error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Balance check
            if (Math.abs(totalDebit - totalCredit) > 0.0001) {
                int choice = JOptionPane.showConfirmDialog(dialog,
                        "The journal entry is unbalanced. Do you want to save anyway?",
                        "Unbalanced entry",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            currentSheetLines = lines;

            try {
                journalRepo.saveJournalEntry(currentSheetLines);
                reloadJournalCards();
                dialog.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog,
                        "Failed to save journal entry.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showSelectAccountDialog() {
        JDialog dialog = new JDialog(this, "Select Account", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(500, 400));

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Table with same columns as CoA (without Utility)
        String[] cols = {"Date", "Time", "Account Name", "Account Type"};
        javax.swing.table.DefaultTableModel model =
                new javax.swing.table.DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        // Load from Chart_of_Accounts
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement("""
                     SELECT account_name, account_type, created_at
                       FROM Chart_of_Accounts
                      WHERE user_id = ?
                      ORDER BY created_at DESC, id DESC
                     """)) {
            Integer userId = Session.getUserId();
            if (userId != null) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
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

                        model.addRow(new Object[]{
                                datePart,
                                timePart,
                                accName,
                                accType
                        });
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton doneBtn = new JButton("Done");
        doneBtn.setBackground(new Color(0x2e6417));
        doneBtn.setForeground(Color.WHITE);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        bottom.add(doneBtn);
        bottom.add(cancelBtn);
        root.add(bottom, BorderLayout.SOUTH);

        // Row click selects account and goes to debit/credit dialog
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    String accName = String.valueOf(model.getValueAt(row, 2));
                    dialog.dispose();
                    showDebitCreditDialog(accName);
                }
            }
        });

        doneBtn.addActionListener(e -> {
            dialog.dispose();
            showSheetDialog();
        });

        cancelBtn.addActionListener(e -> {
            dialog.dispose();
            showSheetDialog();
        });

        dialog.add(root, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showDebitCreditDialog(String accountName) {
        JDialog dialog = new JDialog(this, "Debit or Credit", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setMinimumSize(new Dimension(320, 200));

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel prompt = new JLabel("Select Debit or Credit for: " + accountName);
        prompt.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(prompt);
        root.add(Box.createVerticalStrut(10));

        String[] options = {"Debit", "Credit"};
        JComboBox<String> combo = new JComboBox<>(options);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        root.add(combo);
        root.add(Box.createVerticalStrut(10));

        JTextField amountField = new JTextField();
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        root.add(amountField);
        root.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton doneBtn = new JButton("Done");
        doneBtn.setBackground(new Color(0x2e6417));
        doneBtn.setForeground(Color.WHITE);
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        buttons.add(doneBtn);
        buttons.add(cancelBtn);
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);

        root.add(buttons);

        doneBtn.addActionListener(e -> {
            String choice = (String) combo.getSelectedItem();
            String amtStr = amountField.getText().trim();
            if (choice == null || choice.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please select Debit or Credit.",
                        "Validation error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (amtStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog,
                        "Please enter an amount.",
                        "Validation error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            double value;
            try {
                value = Double.parseDouble(amtStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                        "Invalid amount.",
                        "Validation error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double debit = 0;
            double credit = 0;
            if ("Debit".equals(choice)) {
                debit = value;
            } else {
                credit = value;
            }

            currentSheetLines.add(new JournalEntryRepository.JournalLine(accountName, debit, credit));
            dialog.dispose();
            showSelectAccountDialog();
        });

        cancelBtn.addActionListener(e -> {
            dialog.dispose();
            showSelectAccountDialog();
        });

        dialog.add(root, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void reloadJournalCards() {
        if (entriesListPanel == null) return;
        entriesListPanel.removeAll();

        try {
            java.util.List<JournalEntryRepository.JournalEntry> entries =
                    journalRepo.loadJournalEntriesForCurrentUser();

            for (JournalEntryRepository.JournalEntry entry : entries) {
                JPanel card = new JPanel(new BorderLayout());
                card.setOpaque(false);
                card.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

                RoundedHeaderBox header = new RoundedHeaderBox(new Color(0xcdc6c6));
                header.setLayout(new BorderLayout());
                JLabel title = new JLabel("Journal Entry #" + entry.id + "  " + entry.createdAt);
                title.setFont(new Font("SansSerif", Font.BOLD, 14));
                header.add(title, BorderLayout.WEST);

                card.add(header, BorderLayout.NORTH);

                // Mini-table for lines
                String[] cols = {"Account Name", "Debit", "Credit"};
                javax.swing.table.DefaultTableModel model =
                        new javax.swing.table.DefaultTableModel(cols, 0) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };
                for (JournalEntryRepository.JournalLine line : entry.lines) {
                    model.addRow(new Object[]{
                            line.accountName,
                            line.debit == 0 ? "" : line.debit,
                            line.credit == 0 ? "" : line.credit
                    });
                }

                JTable table = new JTable(model);
                table.setFillsViewportHeight(true);
                JScrollPane scroll = new JScrollPane(table);
                scroll.setPreferredSize(new Dimension(0,
                        Math.min(120, table.getRowHeight() * (entry.lines.size() + 1))));

                card.add(scroll, BorderLayout.CENTER);

                entriesListPanel.add(card);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        entriesListPanel.revalidate();
        entriesListPanel.repaint();
    }

    // ----- Shared rounded components ----------------------------------------

    // Simple rounded box used for "Add new entry", search button and headers
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            journalEntry frame = new journalEntry();
            frame.setVisible(true);
        });
    }
}


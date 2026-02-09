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


// Frame for the Chart of Accounts view
public class CoA extends JFrame {


    private Font workSansBold;
    private Font workSansRegular;
    private static final int METRIC_CARD_WIDTH = 300;
    private static final int METRIC_CARD_HEIGHT = 185;

    private DefaultTableModel accountsTableModel;
    private JTable accountsTable;
    private JLabel accountsCountLabel;
    private final List<Integer> accountIds = new ArrayList<>();

    // Account IDs that have posted transactions (in journal_entry_lines); editing is locked for these
    private final Set<Integer> postedAccountIds = new HashSet<>();

    // Constructor to set up the Chart of Accounts frame with title, default close operation, size, and add the content pane with the createRootPanel method
    public CoA() {
        setTitle("ACCOUNTING SYSTEM - Chart of Accounts");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        setContentPane(createRootPanel());
    }

    // Method to create the main content panel for the Chart of Accounts view
    public JPanel createRootPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xE6E6EB));

        // Side navigation panel on the left and main content area on the right with a top bar and  center area with the accounts table
        JPanel sideNav = createSideNav();
        JPanel mainContent = new JPanel(new BorderLayout(20, 20));
        mainContent.setBackground(new Color(0xE6E6EB));
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        JLabel coaTitle = new JLabel("Chart of Accounts");
        coaTitle.setFont(getWorkSansBold(26f));
        coaTitle.setForeground(new Color(0x2e6417));
        topBar.add(coaTitle, BorderLayout.WEST);

        // Metric card + "Add an Account" button
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        topRow.setOpaque(false);

        // Metric card
        JPanel accountsMetric = createMetricCard("0", "No. of Accounts", "/icon/chart_of_accounts_icon.png");
        accountsMetric.setPreferredSize(new Dimension(300, 185));
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

        // Try to load the plus icon for the button and scale it to fit nicely with the text, if the icon file is missing or cannot be loaded, just show the button without an icon
        try {
            ImageIcon plusIcon = new ImageIcon(getClass().getResource("/icon/plus_icon.png"));
            if (plusIcon.getIconWidth() > 0) {
                Image img = plusIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
                addAccountButton.setIcon(new ImageIcon(img));
                addAccountButton.setHorizontalTextPosition(SwingConstants.RIGHT);
                addAccountButton.setIconTextGap(8);
            }
        } catch (Exception ignored) { }

        // Open the AddAccountDialog when the "Add an Account" button is clicked
        addAccountButton.addActionListener(e -> {
            AddAccountDialog dialog = new AddAccountDialog(this);
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);
        });

        topRow.add(addAccountButton);

        // Center area with the accounts table
        JPanel centerContent = new JPanel(new BorderLayout(20, 20));
        centerContent.setOpaque(false);
        centerContent.add(topRow, BorderLayout.NORTH);
        centerContent.add(createAccountsTablePanel(), BorderLayout.CENTER);

        // Add some spacing around the main content and add the side navigation and main content to the root panel
        mainContent.add(topBar, BorderLayout.NORTH);
        mainContent.add(centerContent, BorderLayout.CENTER);

        // Add the side navigation to the left and the main content to the right of the root panel
        root.add(sideNav, BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);

        return root;
    }

    // Method to log an activity for the current user with the provided details such as activity type, type, and description, and store it in the activity_log table in the database
    private JPanel createSideNav() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(0xcdc6c6));
        side.setLayout(new BorderLayout());

        // Logo panel at the top of the side navigation with the application name
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0xcdc6c6));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        // Application name label
        JLabel appName = new JLabel("ACCOUNTING SYSTEM");
        appName.setFont(getWorkSansBold(16f));
        appName.setForeground(new Color(0x2e6417));
        logoPanel.add(appName);

        // Add some vertical spacing below the logo
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(0xcdc6c6));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Navigation items for the side menu with icons, the Chart of Accounts item is marked as selected since we are in that view
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

        // Add the logo panel to the top of the side navigation, the menu panel in the center, and a logout button panel at the bottom
        side.add(logoPanel, BorderLayout.NORTH);
        side.add(menuPanel, BorderLayout.CENTER);
        side.add(windowManager.createLogoutButtonPanel(), BorderLayout.SOUTH);

        return side;
    }

    // Helper method to create a navigation item for the side menu with the text, selection state, and icon path, and add mouse listeners for hover and click interactions
    private JComponent createNavItem(String text, boolean selected, String iconPath) {
        Color activeColor = new Color(0xB9FF7F);
        Color inactiveColor = new Color(0xcdc6c6);
        Color baseBackground = selected ? activeColor : inactiveColor;

        // Create a RoundedNavItemPanel and set up the layout and styling for the navigation item including the icon and text
        RoundedNavItemPanel item = new RoundedNavItemPanel(baseBackground, activeColor, selected);
        item.setLayout(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        item.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));

        // Content panel to hold the icon and text
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // If the icon file is missing or cannot be loaded, just show the text without an icon
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

        // Label for the navigation item text with styling and add it to the content panel next to the icon
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

            // Handle clicks on the navigation item by checking which item was clicked based on the text and calling the method in the AppWindow to switch views. Use the window that owns this nav item (AppWindow card) to call the methods rather than the standalone CoA frame.
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

    // Rounded container
    private JPanel createAccountsTablePanel() {

        // Outer rounded container
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

        // Create the accounts table with the model and apply styling, set up the Utility column for the Edit and Delete buttons
        accountsTable = StandardTableStyle.createStandardTable(accountsTableModel);
        StandardTableStyle.applyStandardTableStyle(accountsTable);
        accountsTable.getColumnModel().getColumn(4).setPreferredWidth(180);
        accountsTable.getColumnModel().getColumn(4).setCellRenderer(new UtilityButtonRenderer());
        accountsTable.getColumnModel().getColumn(4).setCellEditor(new UtilityButtonEditor());

        // Put the accounts table inside a scroll pane and apply styling to the scroll pane
        JScrollPane scroll = new JScrollPane(accountsTable);
        StandardTableStyle.styleScrollPaneForTable(scroll);

        roundedContainer.add(scroll, BorderLayout.CENTER);

        // Initial data load for the current user
        reloadAccountsTable();

        return roundedContainer;
    }

    // Styling for Edit + Delete buttons in the Utility column
    private class UtilityButtonRenderer implements TableCellRenderer {
        private final JPanel wrapper;

        // Create the Edit and Delete buttons with styling and add them to a wrapper panel
        public UtilityButtonRenderer() {

            // Edit button styling
            RoundedButton editBtn = new RoundedButton("Edit");
            editBtn.setBackground(new Color(0x2e6417));
            editBtn.setForeground(Color.WHITE);
            editBtn.setFont(getWorkSansRegular(12f));
            editBtn.setFocusPainted(false);
            editBtn.setBorderPainted(false);
            editBtn.setContentAreaFilled(false);
            editBtn.setPreferredSize(new Dimension(68, 28));
            editBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Delete button styling
            RoundedButton deleteBtn = new RoundedButton("Delete");
            deleteBtn.setBackground(Color.RED);
            deleteBtn.setForeground(Color.WHITE);
            deleteBtn.setFont(getWorkSansRegular(12f));
            deleteBtn.setFocusPainted(false);
            deleteBtn.setBorderPainted(false);
            deleteBtn.setContentAreaFilled(false);
            deleteBtn.setPreferredSize(new Dimension(82, 28));
            deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            // Put the buttons in a wrapper panel to center them in the cell and add some spacing between them
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

    // Editor for Edit and Delete buttons in the Utility column
    private class UtilityButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final RoundedButton editButton;
        private final RoundedButton deleteButton;
        private final JPanel wrapper;
        private int currentRow = -1;

        // Create the Edit and Delete buttons with styling and add action listeners for handling clicks on the buttons
        public UtilityButtonEditor() {

            // Edit button styling and action listener for handling clicks on the Edit button
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

            // When the Edit button is clicked, check if the account for that row is in the postedAccountIds
            editButton.addActionListener(e -> {

                // If the account is posted, show a dialog that editing is not allowed
                if (currentRow >= 0 && currentRow < accountIds.size()) {
                    if (postedAccountIds.contains(accountIds.get(currentRow))) {
                        showCannotEditPostedDialog();
                    } else {
                        // If the account is not posted, proceed to handle editing the account at that row
                        handleEditAtRow(currentRow);
                    }
                }
                // After handling the edit action, stop editing to close the cell editor and return to the normal table view
                fireEditingStopped();
            });

            // 

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

            // Put the buttons in a wrapper panel to center them in the cell and add some spacing between them
            JPanel inner = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            inner.setOpaque(false);
            inner.add(editButton);
            inner.add(deleteButton);
            wrapper = new JPanel(new GridBagLayout());
            wrapper.setOpaque(false);
            wrapper.add(inner, new GridBagConstraints());
        }

        // When the cell editor is activated for a row, check if that row's account is in the postedAccountIds
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            currentRow = row;

            // If the account for that row is in the postedAccountIds, disable the Edit button and change its tooltip and background color
            boolean posted = row >= 0 && row < accountIds.size() && postedAccountIds.contains(accountIds.get(row));

            // Keep button clickable so the action listener runs and shows the "cannot edit" dialog
            editButton.setEnabled(true);
            editButton.setToolTipText(posted ? "Editing disabled — account has posted transactions" : null);
            editButton.setBackground(posted ? new Color(0x8a9a7a) : new Color(0x2e6417));
            return wrapper;
        }

        // When the cell editor is stopped, reset the currentRow to -1 to indicate that no row is currently being edited
        @Override
        public Object getCellEditorValue() {
            return null;
        }
    }

    // Show a dialog that informs the user that editing is not allowed for accounts that have posted transactions when they attempt to edit such an account
    private void showCannotEditPostedDialog() {

        // Create a modal dialog with the title "Edit Not Allowed"
        JDialog d = new JDialog(this, "Edit Not Allowed", true);
        d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        d.setLayout(new BorderLayout(12, 12));

        // Message label in the center of the dialog with styling
        JLabel msg = new JLabel("<html><div style='text-align:center;'>This account cannot be edited because it already has posted transactions.</div></html>");
        msg.setFont(getWorkSansRegular(14f));
        msg.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        d.add(msg, BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        // OK button to close the dialog
        RoundedButton okBtn = new RoundedButton("OK");
        okBtn.setBackground(new Color(0x2e6417));
        okBtn.setForeground(Color.WHITE);
        okBtn.setFont(getWorkSansRegular(14f));
        okBtn.addActionListener(e -> d.dispose());
        btnPanel.add(okBtn);

        // Add the button panel to the bottom of the dialog, pack the dialog to fit its contents, center it to the parent frame, and make it visible
        d.add(btnPanel, BorderLayout.SOUTH);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    // Handle the editing of an account at the specified row by opening a dialog with a form to edit the account name and type, and saving the changes to the database if the user clicks the Save button
    private void handleEditAtRow(int row) {

        // Check if the row index is valid and if the account for that row is in the postedAccountIds
        if (row < 0 || row >= accountIds.size()) return;

        // Show a dialog that editing is not allowed and return early without proceeding to open the edit dialog
        if (postedAccountIds.contains(accountIds.get(row))) {
            showCannotEditPostedDialog();
            return;
        }

        // If the account is not posted, proceed to open the edit dialog for that account. Get the account ID, current name, and current type for the account at that row to pre-fill the form fields in the edit dialog
        int accountId = accountIds.get(row);
        String currentName = (String) accountsTableModel.getValueAt(row, 2);
        String currentType = (String) accountsTableModel.getValueAt(row, 3);

        // Get the stored account name from the database for the account being edited to compare with the new name in case the user changes the account name, and also to get the original normalized name for updating journal entry lines if the account name is changed
        Integer userId = Session.getUserId();
        if (userId == null) return;
        String storedName = null;

        try (Connection conn = DBConnection.connect();

            // Query the database to get the stored account name for the account being edited using the account ID and user ID to ensure we get the correct account for the current user
             PreparedStatement ps = conn.prepareStatement("SELECT account_name FROM Chart_of_Accounts WHERE id = ? AND user_id = ?")) {
            ps.setInt(1, accountId);
            ps.setInt(2, userId);

            // Execute the query and retrieve the stored account name from the result set
            try (ResultSet rs = ps.executeQuery()) {

                // If a result is found, get the stored account name from the "account_name"
                if (rs.next()) storedName = rs.getString("account_name");
            }

            // If the stored name is null, set it to an empty string to avoid null pointer exceptions
        } catch (SQLException ex) {
            ex.printStackTrace();
            return;
        }
        final String oldNormalized = storedName != null ? storedName.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ") : "";

        // Create a modal dialog for editing the account with the title "Edit Account"
        JDialog dialog = new JDialog(this, "Edit Account", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(12, 12));

        // Create a form panel with a vertical box layout and add labels and input fields for editing the account name and type and filling the fields with the current values for the account being edited
        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Label and text field for account name with styling and filled with the current account name
        JLabel nameLabel = new JLabel("Account Name:");
        nameLabel.setFont(getWorkSansRegular(14f));

        form.add(nameLabel);
        form.add(Box.createVerticalStrut(4));

        // Text field for account name with styling and filled with the current account name
        JTextField nameField = new JTextField(currentName != null ? currentName : "", 24);
        nameField.setFont(getWorkSansRegular(14f));
        nameField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x999999), 1), BorderFactory.createEmptyBorder(6, 8, 6, 8)));

        // Add the account name label and text field to the form with vertical spacing between them
        form.add(nameField);
        form.add(Box.createVerticalStrut(12));

        // Label for account type
        JLabel typeLabel = new JLabel("Account Type:");
        typeLabel.setFont(getWorkSansRegular(14f));

        // Add the account type label to the form with some vertical spacing below it
        form.add(typeLabel);
        form.add(Box.createVerticalStrut(4));

        // Combo box for account type with styling and filled with the current account type, and the options for account types are Asset, Liability, Equity, Revenues, and Expenses
        String[] types = { "Asset", "Liability", "Equity", "Revenues", "Expenses" };
        JComboBox<String> typeCombo = new JComboBox<>(types);
        typeCombo.setFont(getWorkSansRegular(14f));

        // 
        if (currentType != null) {
            
            // Loop through the account type options and set the selected index of the combo box to match the current account type for the account being edited, ignoring case differences
            for (int i = 0; i < types.length; i++) {
                if (currentType.equalsIgnoreCase(types[i])) { typeCombo.setSelectedIndex(i); break; }
            }
        }
        form.add(typeCombo);
        dialog.add(form, BorderLayout.CENTER);

        // Panel for the Save and Cancel buttons at the bottom of the dialog with styling
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        // Save button with styling and action listener for handling the save action when the user clicks the Save button to save the changes to the account in the database
        RoundedButton saveBtn = new RoundedButton("Save");
        saveBtn.setBackground(new Color(0x2e6417));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(getWorkSansRegular(14f));

        // Cancel button with styling and action listener to close the dialog without saving changes when the user clicks the Cancel button
        RoundedButton cancelBtn = new RoundedButton("Cancel");
        cancelBtn.setBackground(Color.GRAY);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(getWorkSansRegular(14f));

        // When the Save button is clicked, validate the input fields, update the account in the database with the new values, log the activity, show a notification, and reload the accounts table to reflect the changes. If there are validation errors or database errors, show appropriate error messages to the user.
        saveBtn.addActionListener(e -> {
            String newName = nameField.getText() != null ? nameField.getText().trim() : "";
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Account name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String newType = (String) typeCombo.getSelectedItem();
            if (newType == null) return;

            // Normalize the new account name by trimming whitespace, converting to uppercase, and replacing multiple spaces with a single space to maintain consistency in how account names are stored and compared in the database
            String normalized = newName.toUpperCase(Locale.ROOT).trim().replaceAll("\\s+", " ");
            String updateSql = "UPDATE Chart_of_Accounts SET account_name = ?, account_type = ? WHERE id = ? AND user_id = ?";
            Integer uid = Session.getUserId();
            if (uid == null) return;

            // Execute the update query to save the changes to the account in the database, and if the account name was changed, also update the account name in any related journal entry lines to maintain consistency. After successfully saving the changes, log the activity, show a notification to the user, and reload the accounts table to reflect the updated information. If there is a database error during this process, show an error message to the user.
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
        
        // When the Cancel button is clicked, simply dispose of the dialog to close it without saving any changes
        cancelBtn.addActionListener(e -> dialog.dispose());
        buttons.add(saveBtn);
        buttons.add(cancelBtn);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // Method to reload the accounts table with the latest data from the database for the current user, including checking which accounts have posted transactions to disable editing for those accounts, and updating the accounts count label with the number of accounts
    private void reloadAccountsTable() {
        if (accountsTableModel == null) {
            return;
        }

        accountsTableModel.setRowCount(0);

        Integer userId = Session.getUserId();
        if (userId == null) {
            return;
        }

        // Query to select the account ID, account name, account type, and creation timestamp for all accounts belonging to the current user from the Chart_of_Accounts table, ordered by creation date and ID in descending order to show the most recently created accounts first
        String sql = """
                SELECT id, account_name, account_type, created_at
                  FROM Chart_of_Accounts
                 WHERE user_id = ?
                 ORDER BY created_at DESC, id DESC
                """;

        Set<String> postedNames = JournalEntryRepository.getPostedAccountNamesNormalized(userId); // Get the set of normalized account names that have posted transactions to check against when loading the accounts to determine which accounts should have editing disabled

        // Execute the query to retrieve the accounts for the current user, and for each account, check if its normalized name is in the set of posted account names to determine if it should be added to the postedAccountIds set. Then add each account's information as a row in the accounts table model with the formatted date, time, account name, account type, and utility buttons. Finally, update the accounts count label with the number of accounts loaded into the table.
        try (Connection conn = DBConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            accountIds.clear();
            postedAccountIds.clear();

            // Execute the query and process the result set to populate the accounts table model with the account information for the current user, while also keeping track of which accounts have posted transactions to disable editing for those accounts
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

                    // Split the createdAt timestamp into date and time parts, and handle cases where the createdAt value might be null or not in the expected format to avoid errors when loading the accounts into the table
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

    //
    private void handleDeleteAtRow(int row) {
        int accountId = accountIds.get(row);
        String accountName = (String) accountsTableModel.getValueAt(row, 2);

        String message = "Are you sure you want to delete Account " + accountName;

        // Create a modal dialog to confirm the deletion of the account with the title "Confirm Delete"
        JDialog dialog = new JDialog(this, "Confirm Delete", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLayout(new BorderLayout(10, 10));

        // Message label in the center of the dialog with styling to confirm if the user wants to delete the account, and add some padding around the label
        JLabel label = new JLabel(message);
        label.setFont(getWorkSansRegular(14f));
        label.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        dialog.add(label, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        // Confirm button to delete the account when the user clicks the Confirm button, and styling for the Confirm button
        JButton confirmBtn = new JButton("Confirm");
        confirmBtn.setBackground(new Color(0x19A64A));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(getWorkSansRegular(14f));

        // Cancel button to close the dialog without deleting the account when the user clicks the Cancel button, and the Confirm button to delete the account from the database, log the activity, show a notification, reload the accounts table, and close the dialog when the user clicks the Confirm button. If there is a database error during deletion, show an error message to the user and do not close the dialog so they can try again or cancel.
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.setFont(getWorkSansRegular(14f));


        // When the Confirm button is clicked, delete the account from the database, log the activity, show a notification, reload the accounts table to reflect the deletion, and close the dialog. If there is a database error during this process, show an error message to the user and do not close the dialog so they can try again or cancel.
        buttonPanel.add(confirmBtn);
        buttonPanel.add(cancelBtn);

        // Add the button panel to the bottom of the dialog, pack the dialog to fit its contents, center it relative to the parent frame, and make it visible
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

        // If the iconPath is provided, attempt to load the icon and add it to the icon panel. If the icon cannot be loaded for any reason, simply skip adding the icon and continue showing the card with just the text.
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

        // Amount label with styling to show the number of accounts, and the description label below it with styling
        accountsCountLabel = new JLabel(amount);
        accountsCountLabel.setFont(getWorkSansBold(34f));
        accountsCountLabel.setForeground(new Color(0x2F2F2F));
        accountsCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Description label with styling to show the "No. of Accounts" text below the amount, and align it to the left
        JLabel descLabel = new JLabel(label);
        descLabel.setFont(getWorkSansRegular(27f));
        descLabel.setForeground(new Color(0x555555));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomContent.add(accountsCountLabel);
        bottomContent.add(descLabel);

        card.add(bottomContent, BorderLayout.CENTER);

        return card;
    }

    // Methods to load Work Sans fonts from resources and provide fallback fonts if the custom fonts cannot be loaded for any reason, ensuring that the application can still display text in a readable font even if the custom fonts are not available
    private Font getWorkSansBold(float size) {
        if (workSansBold != null) {
            return workSansBold.deriveFont(Font.BOLD, size);
        }
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    // Method to load the Work Sans Regular font from the resources and return a derived font with the specified size. If the font cannot be loaded for any reason, return a fallback SansSerif font with the specified size to ensure that text can still be displayed in a readable font.
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

    // Method to load a font from the specified path in the resources and return a derived font with the specified size. If the font cannot be loaded for any reason, return a fallback SansSerif font with the specified size to ensure that text can still be displayed in a readable font.
    private class AddAccountDialog extends JDialog {
        private final Font workSansRegular;
        private final Font workSansBoldLocal;

        private JTextField accountNameField;
        private JComboBox<String> accountTypeCombo;
        private JLabel errorLabel;

        private final ChartOfAccountsRepository repository = new ChartOfAccountsRepository();

        // Constructor for the AddAccountDialog that initializes the dialog with the title "Add an Account", loads the Work Sans fonts specifically for this dialog, and calls the method to initialize the dialog UI components and layout
        public AddAccountDialog(Frame owner) {
            super(owner, "Add an Account", true);

            // Load Work Sans fonts specifically for this dialog
            this.workSansRegular = loadFont("/fonts/Work_Sans/static/WorkSans-Regular.ttf", 14f);
            this.workSansBoldLocal = loadFont("/fonts/Work_Sans/static/WorkSans-Bold.ttf", 14f);

            initDialogUI();
        }

        // Method to initialize the UI components and layout of the AddAccountDialog, including creating the form fields for account name and account type, adding validation error messages, and creating the Save and Cancel buttons with their respective action listeners for handling user interactions in the dialog
        private void initDialogUI() {
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(new Color(0xE6E6EB));

            // Create a card panel with a rounded border and white background to hold the form fields and buttons, and set its layout and padding
            RoundedProfileCard card = new RoundedProfileCard(Color.WHITE);
            card.setLayout(new BorderLayout(0, 16));
            card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

            // Form panel in the center of the card with a vertical box layout to hold the form fields for account name and account type, and add some spacing between the fields
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
            accountNameField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0x999999), 1), BorderFactory.createEmptyBorder(6, 10, 6, 10)));
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

            // Create a combo box for selecting the account type with the options for Asset, Liability, Equity, Revenues, and Expenses, and wrap it in a RoundedInputWrapper to give it a rounded-corner background. Set the font for the combo box and add it to the form panel with some vertical spacing below it.
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

            // Save and Done button to save the new account and close the dialog, with styling for the button
            RoundedButton saveAndDoneBtn = new RoundedButton("Save and Done");
            saveAndDoneBtn.setBackground(new Color(0x19A64A));
            saveAndDoneBtn.setForeground(Color.WHITE);
            saveAndDoneBtn.setFont(workSansRegular.deriveFont(14f));

            // Save and Add More button to save the new account and reset the form fields to allow adding another account, with styling for the button
            RoundedButton saveAndMoreBtn = new RoundedButton("Save and Add More");
            saveAndMoreBtn.setBackground(new Color(0x19A64A));
            saveAndMoreBtn.setForeground(Color.WHITE);
            saveAndMoreBtn.setFont(workSansRegular.deriveFont(14f));

            // Cancel button to close the dialog without saving, with styling for the button
            RoundedButton cancelBtn = new RoundedButton("Cancel");
            cancelBtn.setBackground(Color.RED);
            cancelBtn.setForeground(Color.WHITE);
            cancelBtn.setFont(workSansRegular.deriveFont(14f));

            // When the Save and Done button is clicked, validate the input fields and if validation passes, save the new account to the database, log the activity, reload the accounts table in the main UI, and close the dialog. If there are validation errors or database errors, show appropriate error messages to the user and do not close the dialog so they can correct the errors.
            saveAndDoneBtn.addActionListener(e -> {
                if (validateAndSave(true)) {
                    dispose();
                }
            });

            // When the Save and Add More button is clicked, validate the input fields and if validation passes, save the new account to the database, log the activity, reload the accounts table in the main UI, and reset the form fields to allow adding another account. If there are validation errors or database errors, show appropriate error messages to the user and do not reset the form fields so they can correct the errors.
            saveAndMoreBtn.addActionListener(e -> validateAndSave(false));

            //  When the Cancel button is clicked, simply dispose of the dialog to close it without saving any changes
            cancelBtn.addActionListener(e -> dispose());

            // Add the buttons to the button panel, add the button panel to the bottom of the card, add the card to the root panel, set the root panel as the content pane of the dialog, pack the dialog to fit its contents, and set it to be not resizable
            buttonPanel.add(saveAndDoneBtn);
            buttonPanel.add(saveAndMoreBtn);
            buttonPanel.add(cancelBtn);

            card.add(buttonPanel, BorderLayout.SOUTH);

            root.add(card, BorderLayout.CENTER);
            setContentPane(root);

            pack();
            setResizable(false);
        }

        // Method to validate the input fields for account name and account type, and if validation passes, save the new account to the database, log the activity, and reload the accounts table in the main UI. The method takes a boolean parameter closeAfterSave to determine whether to close the dialog after saving or to reset the form fields for adding another account.
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
            
            // Normalize the account name by trimming whitespace, converting to uppercase, and replacing multiple spaces with a single space to maintain consistency in how account names are stored and compared in the database, and then check if the normalized account name already exists for the current user to prevent duplicate account names. If the account name already exists, show a validation error message to the user. If there is a database error during this process, show an error message to the user indicating that there was an issue saving the account.
            String normalizedName = normalizeAccountName(rawName);
            String accountType = (String) accountTypeCombo.getSelectedItem();
            if (accountType == null || accountType.isBlank()) {
                showValidationError("Please select an account type.");
                return false;
            }

            // Check if the normalized account name already exists for the current user to prevent duplicate account names, and if it does, show a validation error message to the user. If there is a database error during this process, show an error message to the user indicating that there was an issue saving the account.
            try {
                if (repository.accountNameExistsForCurrentUser(normalizedName)) {
                    showValidationError("This account name already exists for your profile.");
                    return false;
                }

                repository.insertAccountForCurrentUser(normalizedName, accountType);
                ActivityLogRepository.log("add", "chart_of_accounts", "Account " + ChartOfAccountsRepository.toTitleCase(normalizedName) + " added");
                reloadAccountsTable();

                // Show a notification to the user that the account was added successfully after saving, and if there is an error while saving, show an error message to the user indicating that there was an issue saving the account.
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        "No logged-in user was found. Please log in again.",
                        "Session Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return false;

                // Catch any SQL exceptions that occur during the database operations and show an error message to the user indicating that there was an issue saving the account, and print the stack trace for debugging purposes.
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "An error occurred while saving the account",
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

        // Method to show a validation error message in the dialog by setting the text of the errorLabel to the provided message, allowing the user to see what validation issue occurred and how to correct it.
        private void showValidationError(String message) {
            errorLabel.setText(message);
        }

        // Method to normalize the account name by trimming leading and trailing whitespace, converting to uppercase, and replacing multiple spaces with a single space to maintain consistency in how account names are stored and compared in the database, ensuring that variations in spacing and case do not result in duplicate account names being created.
        private String normalizeAccountName(String input) {
            String trimmed = input.trim().replaceAll("\\s+", " ");
            return trimmed.toUpperCase(Locale.ROOT);
        }

        // Method to load a font from the specified path in the resources and return a derived font with the specified size. If the font cannot be loaded for any reason, return a fallback SansSerif font with the specified size to ensure that text can still be displayed in a readable font.
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

    // Wrapper panel to give rounded background to input components like JTextField and JComboBox in the add/edit account dialogs, with custom painting to draw the rounded background and border, and layout to properly size and position the inner input component within the wrapper
    private static class RoundedInputWrapper extends JPanel {
        private final int arc = 18;

        // Constructor for the RoundedInputWrapper that takes a JComponent as the inner component to be wrapped with a rounded background. The constructor sets the layout, padding, and border for the wrapper, and also sets the inner component to be non-opaque so that the custom background can be seen behind it. If the inner component is a JTextField or JComboBox, it also sets an empty border on the inner component to provide padding between the text and the edges of the rounded background.
        public RoundedInputWrapper(JComponent inner) {
            setOpaque(false);
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            inner.setOpaque(false);

            // If the inner component is a JTextField or JComboBox, set an empty border on it to provide padding between the text and the edges of the rounded background, ensuring that the text does not touch the edges of the rounded background and maintains a visually appealing layout.
            if (inner instanceof JTextField) {
                ((JTextField) inner).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
            } else if (inner instanceof JComboBox) {
                ((JComboBox<?>) inner).setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
            }

            add(inner, BorderLayout.CENTER);
        }

        // Override the paintComponent method to draw a rounded rectangle as the background of the wrapper, and a border around it. The method uses anti-aliasing for smoother edges, fills the background with white color, and draws a light gray border around it. After custom painting, it calls the superclass's paintComponent method to ensure that any child components are painted correctly on top of the custom background.
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
    
        // Override the paintComponent method to draw a rounded rectangle as the background of the navigation item, changing the color based on whether it is selected or hovered. The method uses anti-aliasing for smoother edges, fills the background with either the hover color or the base color depending on the state, and then calls the superclass's paintComponent method to ensure that any child components are painted correctly on top of the custom background.
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

        // Override the paintComponent method to draw a rounded rectangle as the background of the button, changing the color based on whether the button is pressed or hovered. The method uses anti-aliasing for smoother edges, fills the background with a darker color when pressed, a brighter color when hovered, and the normal background color otherwise. After custom painting, it calls the superclass's paintComponent method to ensure that the button's text and any icons are painted correctly on top of the custom background.
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getBackground();

            // Change the background color based on the button state: if the button is pressed, use a darker color; if it is hovered (rollover), use a brighter color; otherwise, use the normal background color. This provides visual feedback to the user when they interact with the button, making it clear when the button is being pressed or hovered over.
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

        // Override the paintComponent method to draw a rounded rectangle as the background of the card with the specified background color. The method uses anti-aliasing for smoother edges, fills the background with the specified color, and then calls the superclass's paintComponent method to ensure that any child components are painted correctly on top of the custom background.
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

    // Main method to run the Chart of Accounts UI, which creates an instance of the CoA class and makes it visible on the Event Dispatch Thread using SwingUtilities.invokeLater to ensure thread safety when creating and showing the UI.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CoA frame = new CoA();
            frame.setVisible(true);
        });
    }
}

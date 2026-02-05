package com.raven.main;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CoA extends JFrame {

    private Font workSansBold;
    private static final int METRIC_CARD_WIDTH = 300;
    private static final int METRIC_CARD_HEIGHT = 185;

    public CoA() {
        setTitle("ACCOUNTING SYSTEM - Chart of Accounts");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        initUI();
    }

    private void initUI() {
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

        topRow.add(addAccountButton);

        // Put top row at the top, table fills remaining space
        mainContent.add(topRow, BorderLayout.NORTH);
        mainContent.add(createAccountsTablePanel(), BorderLayout.CENTER);

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
                System.out.println("Clicked: " + text);
            }
        });

        return item;
    }

    // Rounded container + styled JTable for the Chart of Accounts (structure only, no data yet)
    private JPanel createAccountsTablePanel() {
        JPanel roundedContainer = new RoundedProfileCard(new Color(0xF5F5F8));
        roundedContainer.setLayout(new BorderLayout());
        roundedContainer.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // Columns only, no rows
        String[] columns = { "Date", "Time", "Account Name", "Account Type", "Utility" };
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(32);                          // slightly taller rows
        table.setShowHorizontalLines(false);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0x19A64A));
        table.setSelectionForeground(Color.WHITE);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        // Alternating row colors & themed selection
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0x19A64A));
        header.setForeground(Color.WHITE);
        header.setFont(getWorkSansBold(14f));
        header.setReorderingAllowed(false);

        Dimension headerSize = header.getPreferredSize();
        headerSize.height = headerSize.height + 8;       // a bit taller than default
        header.setPreferredSize(headerSize);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        roundedContainer.add(scroll, BorderLayout.CENTER);
        return roundedContainer;
    }

    // Alternating row renderer for a clean, modern look
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private final Color evenColor = new Color(0xFFFFFF);
        private final Color oddColor  = new Color(0xF0F0F4);
        private final Color textColor = new Color(0x2F2F2F);
        private final Color selectionBg = new Color(0x19A64A);
        private final Color selectionFg = Color.WHITE;

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {

            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (isSelected) {
                setBackground(selectionBg);
                setForeground(selectionFg);
            } else {
                setBackground((row % 2 == 0) ? evenColor : oddColor);
                setForeground(textColor);
            }

            setBorder(noFocusBorder);
            setHorizontalAlignment(LEFT);
            return this;
        }
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

        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 34));
        amountLabel.setForeground(new Color(0x2F2F2F));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 27));
        descLabel.setForeground(new Color(0x555555));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomContent.add(amountLabel);
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

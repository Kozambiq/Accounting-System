package com.raven.main;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

public class homePage extends JFrame {

    private Font workSansBold;
    private Font workSansRegular;

    // First-row card dimensions (customize length/width)
    private static final int PROFILE_CARD_WIDTH = 350;
    private static final int PROFILE_CARD_HEIGHT = 200;
    private static final int METRIC_CARD_WIDTH = 300;
    private static final int METRIC_CARD_HEIGHT = 140;

    private JLabel revenueAmountLabel;
    private JLabel liabilitiesAmountLabel;
    private JLabel equityAmountLabel;
    private JLabel profileValueLabel;
    private JPanel notificationListPanel;
    private JPanel recentActivityListPanel;

    public homePage() {
        setTitle("ACCOUNTING SYSTEM - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));
        loadWorkSansFonts();
        setContentPane(createRootPanel());
    }

    private void loadWorkSansFonts() {
        try (InputStream boldStream = getClass().getResourceAsStream("/fonts/Work_Sans/static/WorkSans-Bold.ttf")) {
            if (boldStream != null) workSansBold = Font.createFont(Font.TRUETYPE_FONT, boldStream);
        } catch (Exception ignored) { }
        try (InputStream regStream = getClass().getResourceAsStream("/fonts/Work_Sans/static/WorkSans-Regular.ttf")) {
            if (regStream != null) workSansRegular = Font.createFont(Font.TRUETYPE_FONT, regStream);
        } catch (Exception ignored) { }
    }

    /**
     * Build the main dashboard view panel. This is used both as the content
     * pane of this frame and as a card inside a higher-level window manager.
     */
    public JPanel createRootPanel() {
        // Root layout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xE6E6EB));

        // Left navigation
        JPanel sideNav = createSideNav();

        // Main content
        JPanel mainContent = createMainContent();

        root.add(sideNav, BorderLayout.WEST);
        root.add(mainContent, BorderLayout.CENTER);
        return root;
    }

    private JPanel createSideNav() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setBackground(new Color(0xcdc6c6));
        side.setLayout(new BorderLayout());

        // Logo / app name on top
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(0xcdc6c6));
        logoPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.Y_AXIS));

        JLabel appName = new JLabel("ACCOUNTING SYSTEM");
        appName.setFont(getWorkSansBold(16f));
        appName.setForeground(new Color(0x2e6417));

        logoPanel.add(appName);

        // Menu
        JPanel menuPanel = new JPanel();
        menuPanel.setBackground(new Color(0xcdc6c6));
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));

        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        menuPanel.add(createNavItem("DashBoard", true, "/icon/dashboard.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Chart of Accounts", false, "/icon/chart_of_accounts_icon.png"));
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

        // Selected item (e.g. Dashboard) shows the rounded box with active color; others use inactive.
        Color baseBackground = selected ? activeColor : inactiveColor;

        RoundedNavItemPanel item = new RoundedNavItemPanel(baseBackground, activeColor, selected);
        item.setLayout(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        // Left padding increased to make room for icon, right padding stays the same
        Border paddingBorder = BorderFactory.createEmptyBorder(5, 20, 5, 20);
        item.setBorder(paddingBorder);

        // Create a panel to hold icon and text, centered vertically
        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
        contentPanel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Load and add icon if provided
        if (iconPath != null) {
            try {
                ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
                if (icon.getIconWidth() > 0) {
                    // Scale icon to appropriate size (e.g., 20x20)
                    Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                    JLabel iconLabel = new JLabel(new ImageIcon(img));
                    iconLabel.setOpaque(false);
                    iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
                    contentPanel.add(iconLabel);
                    contentPanel.add(Box.createHorizontalStrut(12));
                }
            } catch (Exception e) {
                // Icon not found, continue without it
                System.err.println("Icon not found: " + iconPath);
            }
        }

        JLabel label = new JLabel(text);
        label.setFont(getWorkSansBold(14f));
        label.setForeground(new Color(0x2e6417));
        label.setAlignmentY(Component.CENTER_ALIGNMENT);
        contentPanel.add(label);

        // Center the content panel vertically within the item
        item.add(contentPanel, BorderLayout.CENTER);

        // Make clickable (placeholder for navigation)
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Hover effect: smooth rounded box with same color as Dashboard background.
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
                // Use the actual window that owns this nav item (AppWindow card),
                // not the standalone homePage frame.
                Window window = SwingUtilities.getWindowAncestor(item);
                if ("Chart of Accounts".equals(text)) {
                    windowManager.openChartOfAccounts(window);
                } else if ("Journal Entry".equals(text)) {
                    windowManager.openJournalEntry(window);
                } else if ("Ledger".equals(text)) {
                    windowManager.openLedger(window);
                } else if ("Trial Balance".equals(text)) {
                    windowManager.openTrialBalance(window);
                } else if ("Financial Reports".equals(text)) {
                    windowManager.openFinancialReports(window);
                } else {
                    System.out.println("Clicked: " + text);
                }
            }
        });

        return item;
    }

    private JPanel createMainContent() {
        JPanel main = new JPanel();
        main.setBackground(new Color(0xE6E6EB));
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main.setLayout(new BorderLayout(20, 20));

        // Top title and user chip
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel title = new JLabel("DashBoard");
        title.setFont(getWorkSansBold(26f));
        title.setForeground(new Color(0x2e6417));

        String email = Session.getCurrentUserEmail();
        if (email == null || email.isBlank()) email = "Unknown Email";
        RoundedUserChip userChip = new RoundedUserChip(email);
        userChip.setBackground(new Color(0x39A845));
        userChip.setForeground(Color.WHITE);
        userChip.setFont(getWorkSansBold(16f));
        userChip.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        topBar.add(title, BorderLayout.WEST);
        topBar.add(userChip, BorderLayout.EAST);

        // Center area: cards fixed size; notification/activity anchored to bottom
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout(20, 20));

        JPanel cardsRow = createSummaryRow();
        JPanel bottomRow = createBottomRow();
        bottomRow.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

        center.add(cardsRow, BorderLayout.NORTH);
        center.add(bottomRow, BorderLayout.CENTER);

        main.add(topBar, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    private JPanel createSummaryRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        GridBagLayout gbl = new GridBagLayout();
        row.setLayout(gbl);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 0, 20);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0;
        gbc.weighty = 0;

        // Profile card
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        JPanel profileCard = createProfileCard();
        profileCard.setPreferredSize(new Dimension(PROFILE_CARD_WIDTH, PROFILE_CARD_HEIGHT));
        row.add(profileCard, gbc);

        // Metric cards (values updated by refreshDashboard())
        revenueAmountLabel = new JLabel("₱0");
        liabilitiesAmountLabel = new JLabel("₱0");
        equityAmountLabel = new JLabel("₱0");
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 100, 0, 0);
        gbc.gridx = 1;
        JPanel m1 = createMetricCard(revenueAmountLabel, "Total Revenue", "/icon/asset_icon.png");
        m1.setPreferredSize(new Dimension(METRIC_CARD_WIDTH, METRIC_CARD_HEIGHT));
        row.add(m1, gbc);
        gbc.gridx = 2;
        JPanel m2 = createMetricCard(liabilitiesAmountLabel, "Total Liabilities", "/icon/liabilities_icon.png");
        m2.setPreferredSize(new Dimension(METRIC_CARD_WIDTH, METRIC_CARD_HEIGHT));
        row.add(m2, gbc);
        gbc.gridx = 3;
        JPanel m3 = createMetricCard(equityAmountLabel, "Equity", "/icon/equity_icon.png");
        m3.setPreferredSize(new Dimension(METRIC_CARD_WIDTH, METRIC_CARD_HEIGHT));
        row.add(m3, gbc);

        return row;
    }

    private JPanel createProfileCard() {
        RoundedProfileCard card = new RoundedProfileCard(new Color(0x1D934F));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel iconLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon/dashboard.png"));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) { }
        iconLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Use the full name of the currently logged-in user from Session.
        String fullName = Session.getCurrentUserName();
        if (fullName == null || fullName.isBlank()) {
            fullName = "Unknown User";
        }
        JLabel name = new JLabel(fullName);
        name.setFont(getWorkSansBold(34f));
        name.setForeground(Color.WHITE);

        JLabel currentValueLabel = new JLabel("Current Value");
        currentValueLabel.setFont(getWorkSansRegular(18f));
        currentValueLabel.setForeground(Color.WHITE);

        profileValueLabel = new JLabel("₱0");
        profileValueLabel.setFont(getWorkSansBold(25f));
        profileValueLabel.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(iconLabel);
        textPanel.add(Box.createVerticalStrut(16));
        textPanel.add(name);
        textPanel.add(Box.createVerticalStrut(20));
        textPanel.add(currentValueLabel);
        textPanel.add(profileValueLabel);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMetricCard(JLabel amountLabel, String title, String iconPath) {
        JPanel card = new RoundedProfileCard(new Color(0xcdc6c6));
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

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

        JPanel bottomContent = new JPanel();
        bottomContent.setLayout(new BoxLayout(bottomContent, BoxLayout.Y_AXIS));
        bottomContent.setOpaque(false);
        bottomContent.add(Box.createVerticalGlue());

        amountLabel.setFont(getWorkSansBold(34f));
        amountLabel.setForeground(new Color(0x2F2F2F));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel(title);
        descLabel.setFont(getWorkSansRegular(27f));
        descLabel.setForeground(new Color(0x555555));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottomContent.add(amountLabel);
        bottomContent.add(descLabel);
        card.add(bottomContent, BorderLayout.CENTER);
        return card;
    }

    private JPanel createBottomRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new GridLayout(1, 2, 32, 0));

        row.add(createNotificationPanel());
        row.add(createRecentActivityPanel());

        return row;
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new RoundedProfileCard(new Color(0xcdc6c6));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        headerRow.setOpaque(false);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon/bell_icon.png"));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                headerRow.add(new JLabel(new ImageIcon(img)));
            }
        } catch (Exception ignored) { }
        JLabel header = new JLabel("Notification");
        header.setFont(getWorkSansBold(18f));
        header.setForeground(new Color(0x2F2F2F));
        headerRow.add(header);
        headerRow.add(Box.createHorizontalGlue());
        RoundedButton clearNotifBtn = new RoundedButton("Clear");
        clearNotifBtn.setBackground(new Color(0x545454));
        clearNotifBtn.setForeground(Color.WHITE);
        clearNotifBtn.setFont(getWorkSansBold(12f));
        clearNotifBtn.addActionListener(e -> {
            Integer uid = Session.getUserId();
            if (uid != null) NotificationRepository.clearAll(uid);
            if (notificationListPanel != null) refreshDashboard();
        });
        headerRow.add(clearNotifBtn);

        notificationListPanel = new JPanel();
        notificationListPanel.setOpaque(false);
        notificationListPanel.setLayout(new BoxLayout(notificationListPanel, BoxLayout.Y_AXIS));
        notificationListPanel.setMinimumSize(new Dimension(0, 200));
        JScrollPane notifScroll = new JScrollPane(notificationListPanel);
        notifScroll.setBorder(BorderFactory.createEmptyBorder());
        notifScroll.getViewport().setOpaque(false);
        notifScroll.setOpaque(false);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(notifScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNotificationItem() {
        JPanel item = new RoundedProfileCard(new Color(0xcdc6c6));
        item.setLayout(new BorderLayout());
        item.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        item.setMinimumSize(new Dimension(0, 48));
        item.setPreferredSize(new Dimension(0, 48));
        // Content will be added from backend later
        return item;
    }

    private JPanel createRecentActivityPanel() {
        JPanel panel = new RoundedProfileCard(new Color(0xcdc6c6));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 0));
        headerRow.setOpaque(false);
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon/clock_icon.png"));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
                headerRow.add(new JLabel(new ImageIcon(img)));
            }
        } catch (Exception ignored) { }
        JLabel header = new JLabel("Recent Activity");
        header.setFont(getWorkSansBold(18f));
        header.setForeground(new Color(0x2F2F2F));
        headerRow.add(header);

        recentActivityListPanel = new JPanel();
        recentActivityListPanel.setOpaque(false);
        recentActivityListPanel.setLayout(new BoxLayout(recentActivityListPanel, BoxLayout.Y_AXIS));
        recentActivityListPanel.setMinimumSize(new Dimension(0, 200));
        JScrollPane activityScroll = new JScrollPane(recentActivityListPanel);
        activityScroll.setBorder(BorderFactory.createEmptyBorder());
        activityScroll.getViewport().setOpaque(false);
        activityScroll.setOpaque(false);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(activityScroll, BorderLayout.CENTER);

        return panel;
    }

    private static final int NOTIFICATION_BOX_MIN_HEIGHT = 56;
    private static final int ACTIVITY_BOX_MIN_HEIGHT = 56;
    private static final int BOX_VERTICAL_GAP = 5;
    private static final int BOX_PADDING_TOP_BOTTOM = 10;
    private static final int BOX_PADDING_LEFT_RIGHT = 12;

    public void refreshDashboard() {
        double[] totals = DashboardMetrics.computeTotals();
        double totalRevenue = DashboardMetrics.computeTotalRevenue();
        if (revenueAmountLabel != null) revenueAmountLabel.setText("₱" + formatMetric(totalRevenue));
        if (liabilitiesAmountLabel != null) liabilitiesAmountLabel.setText("₱" + formatMetric(totals[1]));
        if (equityAmountLabel != null) equityAmountLabel.setText("₱" + formatMetric(totals[2]));
        if (profileValueLabel != null) profileValueLabel.setText("₱" + formatMetric(totals[0]));

        if (notificationListPanel != null) {
            notificationListPanel.removeAll();
            Integer userId = Session.getUserId();
            if (userId != null) {
                for (NotificationRepository.NotificationEntry n : NotificationRepository.getAll(userId)) {
                    JPanel box = new RoundedColorBox(new Color(0x545454));
                    box.setLayout(new BorderLayout());
                    box.setBorder(BorderFactory.createEmptyBorder(BOX_PADDING_TOP_BOTTOM, BOX_PADDING_LEFT_RIGHT, BOX_PADDING_TOP_BOTTOM, BOX_PADDING_LEFT_RIGHT));
                    box.setMinimumSize(new Dimension(0, NOTIFICATION_BOX_MIN_HEIGHT));
                    box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
                    box.setAlignmentX(Component.LEFT_ALIGNMENT);
                    String text = n.message + " — " + formatTimeAgo(n.createdAt);
                    JLabel lbl = new JLabel("<html><div style='width:100%'>" + escapeHtml(text) + "</div></html>");
                    lbl.setFont(getWorkSansRegular(15f));
                    lbl.setForeground(Color.WHITE);
                    lbl.setVerticalAlignment(SwingConstants.TOP);
                    box.add(lbl, BorderLayout.CENTER);
                    notificationListPanel.add(box);
                    notificationListPanel.add(Box.createVerticalStrut(BOX_VERTICAL_GAP));
                }
            }
            notificationListPanel.revalidate();
            notificationListPanel.repaint();
        }
        if (recentActivityListPanel != null) {
            recentActivityListPanel.removeAll();
            for (ActivityLogRepository.ActivityEntry e : ActivityLogRepository.getRecent(100_000)) {
                boolean isDeletion = "delete".equalsIgnoreCase(e.activityType) || "remove".equalsIgnoreCase(e.activityType);
                Color bg = isDeletion ? new Color(0xff9999) : new Color(0x99e17a);
                JPanel box = new RoundedColorBox(bg);
                box.setLayout(new BorderLayout());
                box.setBorder(BorderFactory.createEmptyBorder(BOX_PADDING_TOP_BOTTOM, BOX_PADDING_LEFT_RIGHT, BOX_PADDING_TOP_BOTTOM, BOX_PADDING_LEFT_RIGHT));
                box.setMinimumSize(new Dimension(0, ACTIVITY_BOX_MIN_HEIGHT));
                box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
                box.setAlignmentX(Component.LEFT_ALIGNMENT);
                String text = activityDescription(e) + " — " + formatTimeAgo(e.createdAt);
                JLabel lbl = new JLabel("<html><div style='width:100%'>" + escapeHtml(text) + "</div></html>");
                lbl.setFont(getWorkSansRegular(15f));
                lbl.setForeground(new Color(0x1a1a1a));
                lbl.setVerticalAlignment(SwingConstants.TOP);
                box.add(lbl, BorderLayout.CENTER);
                recentActivityListPanel.add(box);
                recentActivityListPanel.add(Box.createVerticalStrut(BOX_VERTICAL_GAP));
            }
            recentActivityListPanel.revalidate();
            recentActivityListPanel.repaint();
        }
    }

    private static String formatMetric(double v) {
        return String.format("%,.2f", Math.abs(v));
    }

    /** Escape for use inside HTML so text doesn't break the label. */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br/>");
    }

    private static String formatTimeAgo(String createdAt) {
        if (createdAt == null || createdAt.isBlank()) return "";
        try {
            java.time.LocalDateTime then = java.time.LocalDateTime.parse(createdAt.replace(" ", "T"),
                    java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            long sec = java.time.Duration.between(then, java.time.LocalDateTime.now()).getSeconds();
            if (sec < 60) return sec + " seconds ago";
            if (sec < 3600) return (sec / 60) + " minutes ago";
            if (sec < 86400) return (sec / 3600) + " hours ago";
            return (sec / 86400) + " days ago";
        } catch (Exception ignored) { return createdAt; }
    }

    private static Color activityColor(String activityType) {
        if (activityType == null) return new Color(0x2F2F2F);
        switch (activityType.toLowerCase()) {
            case "add": case "generate": return new Color(0x2e7d32);
            case "edit": return new Color(0xF9A825);
            case "delete": case "remove": return new Color(0xC62828);
            default: return new Color(0x2F2F2F);
        }
    }

    private static String activityDescription(ActivityLogRepository.ActivityEntry e) {
        return e.description != null && !e.description.isBlank() ? e.description : e.entityType + " " + e.activityType;
    }

    private JPanel createActivitySection(String label) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel chip = new JPanel();
        chip.setBackground(new Color(0xB9FF7F));
        chip.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel chipLabel = new JLabel(label);
        chipLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        chipLabel.setForeground(new Color(0x2F2F2F));
        chip.add(chipLabel);

        section.add(chip);
        section.add(Box.createVerticalStrut(4));
        section.add(createActivityItem());

        return section;
    }

    private JPanel createActivityItem() {
        JPanel bar = new JPanel();
        bar.setBackground(new Color(0x5D5D6B));
        bar.setPreferredSize(new Dimension(0, 12));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
        bar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        return bar;
    }


    private Font getWorkSansBold(float size) {
        if (workSansBold != null) return workSansBold.deriveFont(Font.BOLD, size);
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    private Font getWorkSansRegular(float size) {
        if (workSansRegular != null) return workSansRegular.deriveFont(Font.PLAIN, size);
        return new Font("SansSerif", Font.PLAIN, (int) size);
    }

    // Custom profile card panel with rounded corners
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

    // Custom nav item panel that paints a rounded background to avoid pointy corners on hover
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

            // Selected items always show the rounded box with active color; others show it on hover
            Color fill = (selected || hovered) ? hoverColor : baseColor;
            g2.setColor(fill);
            int arc = 18;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedUserChip extends JButton {
        private static final int ARC = 18;
        RoundedUserChip(String text) {
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
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedColorBox extends JPanel {
        private final Color bgColor;
        RoundedColorBox(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedButton extends JButton {
        private static final int ARC = 12;
        RoundedButton(String text) {
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
            if (getModel().isRollover()) bg = bg.brighter();
            if (getModel().isPressed()) bg = bg.darker();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Small rounded box for metric card badges (e.g. "+ 7.9%")
    private static class RoundedBadgePanel extends JPanel {
        private final Color bgColor;

        public RoundedBadgePanel(Color bgColor) {
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            int arc = 10;
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            homePage page = new homePage();
            page.setVisible(true);
        });
    }
}

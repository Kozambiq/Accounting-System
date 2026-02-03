import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;

public class homePage extends JFrame {

    private Font workSansBold;

    public homePage() {
        setTitle("ACCOUNTING SYSTEM - Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        initUI();
    }

    private void initUI() {
        // Root layout
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(0xE6E6EB));

        // Left navigation
        JPanel sideNav = createSideNav();

        // Main content
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

        menuPanel.add(createNavItem("DashBoard", false, "/icon/dashboard.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Chart of Accounts", false, "/icon/chart_of_accounts.png"));
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(createNavItem("Journal Entry", false, "/icon/journal.png"));
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

        // Default background is the same for all items; selection is reflected only via text color.
        Color baseBackground = inactiveColor;

        RoundedNavItemPanel item = new RoundedNavItemPanel(baseBackground, activeColor);
        item.setLayout(new BorderLayout());
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
                    Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
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
                // TODO: hook up to actual navigation later
                System.out.println("Clicked: " + text);
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
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        title.setForeground(new Color(0x2F2F2F));

        JButton userChip = new JButton("Cyruss Gayola");
        userChip.setFocusPainted(false);
        userChip.setBackground(new Color(0x39A845));
        userChip.setForeground(Color.WHITE);
        userChip.setFont(new Font("SansSerif", Font.BOLD, 14));
        userChip.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        topBar.add(title, BorderLayout.WEST);
        topBar.add(userChip, BorderLayout.EAST);

        // Center area: summary cards and notification/activity
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BorderLayout(20, 20));

        JPanel cardsRow = createSummaryRow();
        JPanel bottomRow = createBottomRow();

        center.add(cardsRow, BorderLayout.NORTH);
        center.add(bottomRow, BorderLayout.CENTER);

        main.add(topBar, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);

        return main;
    }

    private JPanel createSummaryRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new GridLayout(1, 4, 20, 0));

        row.add(createProfileCard());
        row.add(createMetricCard("₱100,000", "Total Assets", "+ 7.9%", new Color(0x19A64A)));
        row.add(createMetricCard("₱100,000", "Total Liabilities", "+ 7.9%", new Color(0xE53935)));
        row.add(createMetricCard("₱100,000", "Total Assets", "+ 7.9%", new Color(0x19A64A)));

        return row;
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel();
        card.setBackground(new Color(0x1D934F));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel name = new JLabel("Cyruss Gayola");
        name.setFont(new Font("SansSerif", Font.BOLD, 18));
        name.setForeground(Color.WHITE);

        JLabel currentValueLabel = new JLabel("Current Value");
        currentValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        currentValueLabel.setForeground(Color.WHITE);

        JLabel value = new JLabel("₱100,000");
        value.setFont(new Font("SansSerif", Font.BOLD, 20));
        value.setForeground(Color.WHITE);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.add(name);
        textPanel.add(Box.createVerticalStrut(16));
        textPanel.add(currentValueLabel);
        textPanel.add(value);

        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createMetricCard(String amount, String label, String changeText, Color changeColor) {
        JPanel card = new JPanel();
        card.setBackground(new Color(0xE0D9E4));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel amountLabel = new JLabel(amount);
        amountLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        amountLabel.setForeground(new Color(0x2F2F2F));

        JLabel descLabel = new JLabel(label);
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(new Color(0x555555));

        JLabel change = new JLabel(changeText);
        change.setFont(new Font("SansSerif", Font.BOLD, 12));
        change.setForeground(Color.WHITE);

        JPanel badge = new JPanel();
        badge.setBackground(changeColor);
        badge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        badge.add(change);

        card.add(badge);
        card.add(Box.createVerticalStrut(16));
        card.add(amountLabel);
        card.add(descLabel);

        return card;
    }

    private JPanel createBottomRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new GridLayout(1, 2, 20, 0));

        row.add(createNotificationPanel());
        row.add(createRecentActivityPanel());

        return row;
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0xE0D9E4));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Notification");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setForeground(new Color(0x2F2F2F));

        JPanel list = new JPanel();
        list.setOpaque(false);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        for (int i = 0; i < 5; i++) {
            list.add(createNotificationItem());
            if (i < 4) {
                list.add(Box.createVerticalStrut(8));
            }
        }

        panel.add(header, BorderLayout.NORTH);
        panel.add(list, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createNotificationItem() {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(new Color(0x5D5D6B));
        item.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel text = new JLabel("Journal Entry \"Entry Name\" is unbalanced");
        text.setFont(new Font("SansSerif", Font.PLAIN, 13));
        text.setForeground(Color.WHITE);

        JPanel dot = new JPanel();
        dot.setPreferredSize(new Dimension(14, 14));
        dot.setBackground(new Color(0xFF6B6B));
        dot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2, true));

        item.add(text, BorderLayout.CENTER);
        item.add(dot, BorderLayout.EAST);

        return item;
    }

    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(0xE0D9E4));
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel header = new JLabel("Recent Activity");
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        header.setForeground(new Color(0x2F2F2F));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createActivitySection("Today"));
        content.add(Box.createVerticalStrut(16));
        content.add(createActivitySection("Yesterday"));

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createActivitySection(String label) {
        JPanel section = new JPanel();
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));

        JPanel chip = new JPanel();
        chip.setBackground(new Color(0xB9FF7F));
        chip.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        JLabel chipLabel = new JLabel(label);
        chipLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        chipLabel.setForeground(new Color(0x2F2F2F));
        chip.add(chipLabel);

        section.add(chip);
        section.add(Box.createVerticalStrut(8));

        for (int i = 0; i < 4; i++) {
            section.add(createActivityItem());
            if (i < 3) {
                section.add(Box.createVerticalStrut(6));
            }
        }

        return section;
    }

    private JPanel createActivityItem() {
        JPanel bar = new JPanel();
        bar.setBackground(new Color(0x5D5D6B));
        bar.setPreferredSize(new Dimension(0, 18));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        bar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return bar;
    }


    private Font getWorkSansBold(float size) {
        if (workSansBold != null) {
            return workSansBold.deriveFont(Font.BOLD, size);
        }
        // Graceful fallback if the font resource is missing
        return new Font("SansSerif", Font.BOLD, (int) size);
    }

    // Custom nav item panel that paints a rounded background to avoid pointy corners on hover
    private static class RoundedNavItemPanel extends JPanel {
        private final Color baseColor;
        private final Color hoverColor;
        private boolean hovered;

        public RoundedNavItemPanel(Color baseColor, Color hoverColor) {
            this.baseColor = baseColor;
            this.hoverColor = hoverColor;
            setOpaque(false);
        }

        public void setHovered(boolean hovered) {
            this.hovered = hovered;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = hovered ? hoverColor : baseColor;
            g2.setColor(fill);
            int arc = 18;
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

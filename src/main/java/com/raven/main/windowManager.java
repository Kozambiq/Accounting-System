package com.raven.main;

import javax.swing.*;
import java.awt.*;

public class windowManager extends JFrame {

    /** Creates a rounded Logout button panel for the left nav (SOUTH). Clears session and shows login. */
    public static JPanel createLogoutButtonPanel() {
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        wrap.setBackground(new Color(0xcdc6c6));
        wrap.setBorder(BorderFactory.createEmptyBorder(12, 16, 20, 16));

        JButton btn = new JButton("Logout") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getBackground();
                if (getModel().isRollover()) bg = bg.brighter();
                if (getModel().isPressed()) bg = bg.darker();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBackground(new Color(0x2e6417));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(200, 48));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        try {
            ImageIcon icon = new ImageIcon(windowManager.class.getResource("/icon/leave_icon.png"));
            if (icon.getIconWidth() > 0) {
                Image img = icon.getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH);
                btn.setIcon(new ImageIcon(img));
            }
        } catch (Exception ignored) { }
        btn.addActionListener(e -> {
            Session.clear();
            Window w = SwingUtilities.getWindowAncestor(btn);
            if (w != null) w.dispose();
            new windowManager().setVisible(true);
        });
        wrap.add(btn);
        return wrap;
    }

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public static final String LOGIN_CARD = "login";
    public static final String SIGNUP_CARD = "signup";
    public static final String RECOVERY_CARD = "recovery";

    public windowManager() {
        setTitle("Accounting System - Authentication");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(new logInPage(), LOGIN_CARD);
        cardPanel.add(new signUpPage(), SIGNUP_CARD);
        cardPanel.add(new accountRecovery(), RECOVERY_CARD);

        setContentPane(cardPanel);
        cardLayout.show(cardPanel, LOGIN_CARD);
        setVisible(true);
    }

    public void showLogin() {
        cardLayout.show(cardPanel, LOGIN_CARD);
    }

    public void showSignup() {
        cardLayout.show(cardPanel, SIGNUP_CARD);
    }

    public void showAccountRecovery() {
        cardLayout.show(cardPanel, RECOVERY_CARD);
        cardPanel.revalidate();
        cardPanel.repaint();
    }

    /**
     * Open the main dashboard (homePage) after a successful and verified login.
     * This disposes the authentication window and shows the main app window
     * (with Dashboard and Chart of Accounts cards) in a new frame.
     */
    public void openHomePage() {
        SwingUtilities.invokeLater(() -> {
            AppWindow app = new AppWindow();
            app.setVisible(true);
            dispose();
        });
    }

    /**
     * Helper used by other top-level pages (e.g. Dashboard) to transition
     * to the Chart of Accounts screen while preserving the current session.
     *
     * @param current a window to dispose after opening CoA, or {@code null}
     *                to leave existing windows untouched.
     */
    public static void openChartOfAccounts(Window current) {
        if (current instanceof AppWindow) {
            ((AppWindow) current).showChartOfAccounts();
        }
    }

    /**
     * Helper used by other top-level pages to transition to the Journal Entry
     * screen while preserving the current session.
     */
    public static void openJournalEntry(Window current) {
        if (current instanceof AppWindow) {
            ((AppWindow) current).showJournalEntry();
        }
    }

    /**
     * Helper used by other top-level pages to transition to the Ledger
     * screen while preserving the current session.
     */
    public static void openLedger(Window current) {
        if (current instanceof AppWindow) {
            ((AppWindow) current).showLedger();
        }
    }

    /**
     * Helper used by other top-level pages to transition to the Trial Balance
     * screen while preserving the current session.
     */
    public static void openTrialBalance(Window current) {
        if (current instanceof AppWindow) {
            ((AppWindow) current).showTrialBalance();
        }
    }

    /**
     * Helper used by other top-level pages to transition to the Financial Reports
     * screen while preserving the current session.
     */
    public static void openFinancialReports(Window current) {
        if (current instanceof AppWindow) {
            ((AppWindow) current).showFinancialReports();
        }
    }

    public static void main(String[] args) {
        // Ensure database schema is present before any UI or queries run.
        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(windowManager::new);
    }
}


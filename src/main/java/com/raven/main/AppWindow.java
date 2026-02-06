package com.raven.main;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window after authentication.
 *
 * Uses a CardLayout to switch between the dashboard (homePage view) and the
 * Chart of Accounts (CoA view), giving smooth transitions similar to the
 * login/signup flow while preserving the current Session.
 */
public class AppWindow extends JFrame {

    public static final String CARD_DASHBOARD = "dashboard";
    public static final String CARD_COA = "coa";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    public AppWindow() {
        setTitle("ACCOUNTING SYSTEM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Dashboard view card
        homePage dashboardFrame = new homePage();
        JPanel dashboardView = dashboardFrame.createRootPanel();

        // Chart of Accounts view card
        CoA coaFrame = new CoA();
        JPanel coaView = coaFrame.createRootPanel();

        cardPanel.add(dashboardView, CARD_DASHBOARD);
        cardPanel.add(coaView, CARD_COA);

        setContentPane(cardPanel);
        showDashboard();
    }

    public void showDashboard() {
        cardLayout.show(cardPanel, CARD_DASHBOARD);
    }

    public void showChartOfAccounts() {
        cardLayout.show(cardPanel, CARD_COA);
    }
}


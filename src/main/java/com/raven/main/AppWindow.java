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
    public static final String CARD_JOURNAL = "journal";
    public static final String CARD_LEDGER = "ledger";
    public static final String CARD_TRIAL_BALANCE = "trialBalance";

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

        // Journal Entry view card
        journalEntry journalFrame = new journalEntry();
        JPanel journalView = (JPanel) journalFrame.getContentPane();

        // Ledger view card
        ledger ledgerFrame = new ledger();
        JPanel ledgerView = (JPanel) ledgerFrame.getContentPane();

        // Trial Balance view card
        trialBalance trialBalanceFrame = new trialBalance();
        JPanel trialBalanceView = (JPanel) trialBalanceFrame.getContentPane();

        cardPanel.add(dashboardView, CARD_DASHBOARD);
        cardPanel.add(coaView, CARD_COA);
        cardPanel.add(journalView, CARD_JOURNAL);
        cardPanel.add(ledgerView, CARD_LEDGER);
        cardPanel.add(trialBalanceView, CARD_TRIAL_BALANCE);

        setContentPane(cardPanel);
        showDashboard();
    }

    public void showDashboard() {
        cardLayout.show(cardPanel, CARD_DASHBOARD);
    }

    public void showChartOfAccounts() {
        cardLayout.show(cardPanel, CARD_COA);
    }

    public void showJournalEntry() {
        cardLayout.show(cardPanel, CARD_JOURNAL);
    }

    public void showLedger() {
        cardLayout.show(cardPanel, CARD_LEDGER);
    }

    public void showTrialBalance() {
        cardLayout.show(cardPanel, CARD_TRIAL_BALANCE);
    }
}


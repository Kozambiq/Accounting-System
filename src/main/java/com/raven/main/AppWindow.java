package com.raven.main;

import javax.swing.*;
import java.awt.*;

// Main application window that manages different views (dashboard, chart of accounts, journal entry, ledger, trial balance, financial reports) using a CardLayout
public class AppWindow extends JFrame {

    // Constants for card names to identify different views in the CardLayout
    public static final String CARD_DASHBOARD = "dashboard";
    public static final String CARD_COA = "coa";
    public static final String CARD_JOURNAL = "journal";
    public static final String CARD_LEDGER = "ledger";
    public static final String CARD_TRIAL_BALANCE = "trialBalance";
    public static final String CARD_FINANCIAL_REPORTS = "financialReports";

    // CardLayout to switch between different views and a main panel that holds the cards
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final homePage dashboardFrame;
    private final ledger ledgerFrame;
    private final trialBalance trialBalanceFrame;
    private final financialReports financialReportsFrame;

    // Constructor to set up the main application window
    public AppWindow() {

        // Set up the main application window with title, default close operation, size, and initialize the CardLayout and different view cards
        setTitle("ACCOUNTING SYSTEM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1200, 700));

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Dashboard view card
        dashboardFrame = new homePage();
        JPanel dashboardView = dashboardFrame.createRootPanel();

        // Chart of Accounts view card
        CoA coaFrame = new CoA();
        JPanel coaView = coaFrame.createRootPanel();

        // Journal Entry view card
        journalEntry journalFrame = new journalEntry();
        JPanel journalView = (JPanel) journalFrame.getContentPane();

        // Ledger view card
        ledgerFrame = new ledger();
        JPanel ledgerView = (JPanel) ledgerFrame.getContentPane();

        // Trial Balance view card (needs reference to Ledger for data)
        trialBalanceFrame = new trialBalance(ledgerFrame);
        JPanel trialBalanceView = (JPanel) trialBalanceFrame.getContentPane();

        // Financial Reports view card (Income Statement, Cash Flow, Balance Sheet)
        financialReportsFrame = new financialReports(ledgerFrame);
        JPanel financialReportsView = (JPanel) financialReportsFrame.getContentPane();

        // When Ledger mini cards change, refresh Trial Balance and dashboard metrics/activity
        ledgerFrame.setOnLedgerChange(() -> {
            trialBalanceFrame.refreshTrialBalanceFromLedger();
            dashboardFrame.refreshDashboard();
        });

        // Add all the different view cards to the main card panel
        cardPanel.add(dashboardView, CARD_DASHBOARD);
        cardPanel.add(coaView, CARD_COA);
        cardPanel.add(journalView, CARD_JOURNAL);
        cardPanel.add(ledgerView, CARD_LEDGER);
        cardPanel.add(trialBalanceView, CARD_TRIAL_BALANCE);
        cardPanel.add(financialReportsView, CARD_FINANCIAL_REPORTS);

        setContentPane(cardPanel);
        showDashboard();
    }

    // Methods to switch between different views in the CardLayout and refresh data when switching to the dashboard
    public void showDashboard() {
        cardLayout.show(cardPanel, CARD_DASHBOARD);
        dashboardFrame.refreshDashboard();
    }

    // Method to show the Chart of Accounts view when selected from the sidebar menu
    public void showChartOfAccounts() {
        cardLayout.show(cardPanel, CARD_COA);
    }

    // Method to show the Journal Entry view and refresh its data when displayed
    public void showJournalEntry() {
        cardLayout.show(cardPanel, CARD_JOURNAL);
    }

    // Method to show the Ledger view and refresh its data when displayed
    public void showLedger() {
        cardLayout.show(cardPanel, CARD_LEDGER);
    }

    // Method to show the Trial Balance view and refresh its data from the Ledger when displayed
    public void showTrialBalance() {
        cardLayout.show(cardPanel, CARD_TRIAL_BALANCE);
    }

    // Method to show the Financial Reports view and refresh its data from the Ledger when displayed
    public void showFinancialReports() {
        cardLayout.show(cardPanel, CARD_FINANCIAL_REPORTS);
    }

    // Getter for the Ledger frame to allow other views (like Trial Balance and Financial Reports) to access latest ledger data for calculations and display
    public ledger getLedgerFrame() {
        return ledgerFrame;
    }
}


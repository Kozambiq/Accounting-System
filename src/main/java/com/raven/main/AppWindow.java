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
    public static final String CARD_FINANCIAL_REPORTS = "financialReports";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final ledger ledgerFrame;
    private final trialBalance trialBalanceFrame;
    private final financialReports financialReportsFrame;

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
        ledgerFrame = new ledger();
        JPanel ledgerView = (JPanel) ledgerFrame.getContentPane();

        // Trial Balance view card (needs reference to Ledger for data)
        trialBalanceFrame = new trialBalance(ledgerFrame);
        JPanel trialBalanceView = (JPanel) trialBalanceFrame.getContentPane();

        // Financial Reports view card (Income Statement, Cash Flow, Balance Sheet)
        financialReportsFrame = new financialReports(ledgerFrame);
        JPanel financialReportsView = (JPanel) financialReportsFrame.getContentPane();

        // When Ledger mini cards change, refresh Trial Balance table if it was generated
        ledgerFrame.setOnLedgerChange(() -> trialBalanceFrame.refreshTrialBalanceFromLedger());

        cardPanel.add(dashboardView, CARD_DASHBOARD);
        cardPanel.add(coaView, CARD_COA);
        cardPanel.add(journalView, CARD_JOURNAL);
        cardPanel.add(ledgerView, CARD_LEDGER);
        cardPanel.add(trialBalanceView, CARD_TRIAL_BALANCE);
        cardPanel.add(financialReportsView, CARD_FINANCIAL_REPORTS);

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

    public void showFinancialReports() {
        cardLayout.show(cardPanel, CARD_FINANCIAL_REPORTS);
    }

    public ledger getLedgerFrame() {
        return ledgerFrame;
    }
}


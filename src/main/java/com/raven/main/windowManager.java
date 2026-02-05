package com.raven.main;

import javax.swing.*;
import java.awt.*;

public class windowManager extends JFrame {

    private CardLayout cardLayout;
    private JPanel cardPanel;

    public static final String LOGIN_CARD = "login";
    public static final String SIGNUP_CARD = "signup";

    public windowManager() {
        setTitle("Accounting System - Authentication");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        cardPanel.add(new logInPage(), LOGIN_CARD);
        cardPanel.add(new signUpPage(), SIGNUP_CARD);

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

    /**
     * Open the main dashboard (homePage) after a successful and verified login.
     * This disposes the authentication window and shows the dashboard in a new frame.
     */
    public void openHomePage() {
        SwingUtilities.invokeLater(() -> {
            homePage page = new homePage();
            page.setVisible(true);
            dispose();
        });
    }

    public static void main(String[] args) {
        // Ensure database schema is present before any UI or queries run.
        DatabaseInitializer.initialize();
        SwingUtilities.invokeLater(windowManager::new);
    }
}


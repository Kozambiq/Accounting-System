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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(windowManager::new);
    }
}


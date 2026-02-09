package com.raven.main;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

    // Account Recovery Panel for resetting user passwords
public class accountRecovery extends JPanel {

    private RoundedPanel card;
    private Font workSansRegular;
    private Font workSansBold;

    private IconTextField emailField;
    private TogglePasswordField passwordField;
    private TogglePasswordField confirmPasswordField;
    private RoundedButton confirmBtn;

    public accountRecovery() {
        setLayout(new BorderLayout());

        // Load custom fonts
        workSansRegular = loadFont("/fonts/Work_Sans/static/WorkSans-Regular.ttf", 14f);
        workSansBold = loadFont("/fonts/Work_Sans/static/WorkSans-Bold.ttf", 14f);

        // Background
        BackgroundPanel backgroundPanel = new BackgroundPanel("/icon/bg3.jpg");
        add(backgroundPanel, BorderLayout.CENTER);

        card = new RoundedPanel();
        card.setLayout(new MigLayout("wrap 1, gapy 4, align center, top 50"));
        backgroundPanel.add(card, "center, w 400!");

        // Logo
        Image logoImg = new ImageIcon(getClass().getResource("/icon/logo.png")).getImage();
        int targetLogoHeight = 160;
        int logoW = logoImg.getWidth(null);
        int logoH = logoImg.getHeight(null);
        if (logoW > 0 && logoH > 0) {
            int scaledW = logoW * targetLogoHeight / logoH;
            logoImg = logoImg.getScaledInstance(scaledW, targetLogoHeight, Image.SCALE_SMOOTH);
        }
        JLabel logoLabel = new JLabel(new ImageIcon(logoImg));
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, "alignx center, wrap 0");

        // Title and Subtitle
        JLabel title = new JLabel("Account Recovery");
        title.setFont(workSansBold.deriveFont(36f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "align center, span, wrap 0");

        JLabel subtitle = new JLabel("Enter the email of the account you wish to recover");
        subtitle.setFont(workSansRegular.deriveFont(14f));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(subtitle, "align center, span, wrap 20");

        // Input fields with icons
        Image emailIcon = new ImageIcon(getClass().getResource("/icon/email.png")).getImage();
        Image passwordIcon = new ImageIcon(getClass().getResource("/icon/key.png")).getImage();
        Image eyeOpen = new ImageIcon(getClass().getResource("/icon/eyeOpen.png")).getImage();
        Image eyeClosed = new ImageIcon(getClass().getResource("/icon/eyeClosed.png")).getImage();

        // Email field
        emailField = new IconTextField("Email", emailIcon);
        emailField.setFont(workSansRegular.deriveFont(14f));
        card.add(emailField, "w 320!, h 40!, align center");

        // Password field
        passwordField = new TogglePasswordField("Password", passwordIcon, eyeOpen, eyeClosed);
        passwordField.setFont(workSansRegular.deriveFont(14f));
        card.add(passwordField, "w 320!, h 40!, align center, wrap 4");

        // Confirm Password field
        confirmPasswordField = new TogglePasswordField("Confirm Password", passwordIcon, eyeOpen, eyeClosed);
        confirmPasswordField.setFont(workSansRegular.deriveFont(14f));
        card.add(confirmPasswordField, "w 320!, h 40!, align center, wrap 20");

        // Confirm button
        confirmBtn = new RoundedButton("Reset Password");
        confirmBtn.setFont(workSansBold.deriveFont(20f));
        confirmBtn.setBackground(new Color(33, 150, 243));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        confirmBtn.setEnabled(false);
        card.add(confirmBtn, "w 320!, h 40!, align center, wrap 20");

        // Enable confirm button only when passwords match and are not empty
        updateConfirmButtonState();
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
        });
        confirmPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateConfirmButtonState(); }
        });

        confirmBtn.addActionListener(e -> performReset());

        // Footer with back to login link
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);
        JLabel footerText = new JLabel("Remember your password?");
        footerText.setFont(workSansRegular.deriveFont(12f));
        JLabel backLink = new JLabel(
                "<html><span style='color:#2196F3; text-decoration:underline;'>Back to Log In</span></html>");
        backLink.setFont(workSansRegular.deriveFont(12f));
        backLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                Window window = SwingUtilities.getWindowAncestor(accountRecovery.this);
                if (window instanceof windowManager) {
                    ((windowManager) window).showLogin();
                }
            }
        });
        footerPanel.add(footerText);
        footerPanel.add(backLink);
        card.add(footerPanel, "align center, wrap, gapbottom 30");
    }

    // Enable the confirm button only when password fields are not empty and match
    private void updateConfirmButtonState() {
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());
        boolean match = !pwd.isEmpty() && pwd.equals(confirm);
        confirmBtn.setEnabled(match);
    }

    // Perform the password reset operation
    private void performReset() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmPasswordField.getPassword());

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your email.", "Required field",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a new password.", "Required field",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password and Confirm Password do not match.", "Validation",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        confirmBtn.setEnabled(false);

        // Run database operations in a background
        new Thread(() -> {
            boolean success = false;
            String errorMessage = null;

            // Check if email exists and update password
            try (Connection conn = DBConnection.connect()) {
                try (PreparedStatement check = conn.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                    check.setString(1, email);
                    try (ResultSet rs = check.executeQuery()) {
                        if (!rs.next()) {
                            errorMessage = "No account found with this email.";
                        } else {
                            try (PreparedStatement update = conn.prepareStatement(
                                    "UPDATE users SET password = ? WHERE email = ?")) {
                                update.setString(1, password);
                                update.setString(2, email);
                                update.executeUpdate();
                                success = true;
                            }
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                errorMessage = "database error occurred";
            }

            final boolean ok = success;
            final String err = errorMessage;

            // Update UI on the Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                confirmBtn.setEnabled(true);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                            "Password changed successfully. Please log in with your new password.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    Window window = SwingUtilities.getWindowAncestor(accountRecovery.this);
                    if (window instanceof windowManager) {
                        ((windowManager) window).showLogin();
                    }
                } else if (err != null) {
                    JOptionPane.showMessageDialog(this, err, "Account Recovery", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    // Load custom font from resources
    private Font loadFont(String path, float size) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                return font.deriveFont(size);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Font("Arial", Font.PLAIN, (int) size);
    }

    // Custom panel with rounded corners and layered borders
    private static class RoundedPanel extends JPanel {
        private final int cornerRadius = 20;

        RoundedPanel() {
            setOpaque(false);
        }

        // Draw layered borders and transparent background
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();
            for (int i = 6; i > 0; i--) {
                float alpha = i * 0.06f;
                g2.setColor(new Color(255, 255, 255, (int) (alpha * 255)));
                g2.setStroke(new BasicStroke(i));
                g2.drawRoundRect(i, i, w - i * 2, h - i * 2, cornerRadius, cornerRadius);
            }
            g2.setColor(new Color(255, 255, 255, 140));
            g2.fillRoundRect(6, 6, w - 12, h - 12, cornerRadius, cornerRadius);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(6, 6, w - 12, h - 12, cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Panel that draws a background image scaled to fit
    private static class BackgroundPanel extends JPanel {
        private final Image backgroundImage;

        // Load background image from resources
        BackgroundPanel(String resourcePath) {
            backgroundImage = new ImageIcon(accountRecovery.class.getResource(resourcePath)).getImage();
            setLayout(new MigLayout("fill, center", "[grow]", "[grow]"));
        }

        // Draw the background image scaled to fill the panel
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Custom JTextField with icon and placeholder text
    private class IconTextField extends JTextField {
        private final String placeholder;
        private final Image icon;
        private final int arc = 20, iconSize = 20, iconPadding = 10;
        private boolean focused;

        // Implemented the text field with placeholder and icon and add focus listeners for styling
        IconTextField(String placeholder, Image icon) {
            this.placeholder = placeholder;
            this.icon = icon;
            setBorder(null);
            setOpaque(false);
            setCaretColor(Color.BLACK);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                // When focus is lost, update and repaint to show the unfocused style
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
        }

        // Inset to provide space for the icon on the left
        @Override
        public Insets getInsets() {
            Insets insets = super.getInsets();
            return new Insets(insets.top, iconSize + iconPadding + 5, insets.bottom, insets.right);
        }

        // 
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            if (focused) {
                g2.setColor(new Color(33, 150, 243, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            }
            super.paintComponent(g2);
            if (icon != null) g2.drawImage(icon, 10, (getHeight() - iconSize) / 2, iconSize, iconSize, this);
            if (getText().isEmpty() && workSansRegular != null) {
                g2.setFont(workSansRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize + 10 + iconPadding,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
            }
            g2.dispose();
        }
    }

    // Custom JPasswordField that includes icon, placeholder text, and toggle for showing/hiding the password
    private class TogglePasswordField extends JPasswordField {
        private final String placeholder;
        private final Image leftIcon, eyeOpen, eyeClosed;
        private boolean showPassword;
        private final int arc = 20, iconSize = 20, iconPadding = 10;
        private boolean focused;

        // Added the password field with placeholder, left icon, and eye icon for toggling visibility. Added listeners for focus and mouse events to handle styling and toggling.
        TogglePasswordField(String placeholder, Image leftIcon, Image eyeOpen, Image eyeClosed) {
            this.placeholder = placeholder;
            this.leftIcon = leftIcon;
            this.eyeOpen = eyeOpen;
            this.eyeClosed = eyeClosed;
            setBorder(null);
            setOpaque(false);
            setEchoChar('\u2022');
            setCaretColor(Color.BLACK);
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    focused = true;
                    repaint();
                }

                // When focus is lost, update and repaint to show the unfocused style
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    focused = false;
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getX() >= getWidth() - iconSize - iconPadding) {
                        showPassword = !showPassword;
                        setEchoChar(showPassword ? (char) 0 : '\u2022');
                        repaint();
                    }
                }
            });
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    setCursor(e.getX() >= getWidth() - iconSize - iconPadding
                            ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                            : Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                }
            });
        }

        // Inset to provide space for the left icon and the eye toggle icon on the right
        @Override
        public Insets getInsets() {
            Insets insets = super.getInsets();
            int left = iconSize + iconPadding + 5;
            int right = iconSize + iconPadding + 5;
            return new Insets(insets.top, left, insets.bottom, right);
        }

        // Custom painting to draw the rounded background, left icon, eye toggle icon, and placeholder text when empty. Also draws a focus border when focused.
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 200));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            if (focused) {
                g2.setColor(new Color(33, 150, 243, 100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, arc, arc);
            }
            super.paintComponent(g2);
            if (leftIcon != null)
                g2.drawImage(leftIcon, 10, (getHeight() - iconSize) / 2, iconSize, iconSize, this);
            if (getPassword().length == 0 && workSansRegular != null) {
                g2.setFont(workSansRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize + 10 + iconPadding,
                        getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);
            }
            Image eyeIcon = showPassword ? eyeOpen : eyeClosed;
            if (eyeIcon != null)
                g2.drawImage(eyeIcon, getWidth() - iconSize - iconPadding, (getHeight() - iconSize) / 2,
                        iconSize, iconSize, this);
            g2.dispose();
        }
    }

    // Custom JButton with rounded corners and hover/press effects
    private static class RoundedButton extends JButton {
        private final int arc = 20;

        RoundedButton(String text) {
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
        }

        // Changes background color based on button state and centers the text.
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground();
            if (getModel().isPressed()) bg = bg.darker();
            else if (getModel().isRollover()) bg = bg.brighter();
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 2;
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }
}

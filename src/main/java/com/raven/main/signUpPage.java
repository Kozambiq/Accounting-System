package com.raven.main;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import net.miginfocom.swing.MigLayout;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.regex.Pattern;

public class signUpPage extends JPanel {

    private RoundedPanel card;

    // Merriweather fonts
    private Font merriweatherRegular;
    private Font merriweatherBold;
    private Font merriweatherItalic;
    private Font merriweatherBoldItalic;

    // Error labels
    private JLabel firstNameError;
    private JLabel lastNameError;
    private JLabel emailError;
    private JLabel confirmPasswordError;

    public signUpPage() {
        setLayout(new BorderLayout());

        // Load fonts
        merriweatherRegular = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Regular.ttf", 14f);
        merriweatherBold = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Bold.ttf", 14f);
        merriweatherItalic = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Italic.ttf", 14f);
        merriweatherBoldItalic = loadFont("/fonts/Merriweather/static/Merriweather_120pt-BoldItalic.ttf", 14f);

        // Background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel("/icon/bg3.jpg");
        add(backgroundPanel, BorderLayout.CENTER);

        // Card with top padding to push content up
        card = new RoundedPanel();
        card.setLayout(new MigLayout("wrap 1, gapy 4, align center, top 50"));
        card.setPreferredSize(new Dimension(400, 720));
        backgroundPanel.add(card, "center");

        // Logo (scaled down, centered, aspect ratio preserved)
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
        card.add(logoLabel, "alignx center, wrap, gapbottom 10");

        // Title
        JLabel title = new JLabel("Sign Up");
        title.setFont(merriweatherBold.deriveFont(36f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "align center, wrap 0");

        JLabel subtitle = new JLabel("Create your account");
        subtitle.setFont(merriweatherRegular.deriveFont(14f));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(subtitle, "align center, wrap 15");

        // Icons
        Image userIcon = loadImage("/icon/user.png");
        Image emailIcon = loadImage("/icon/email.png");
        Image passwordIcon = loadImage("/icon/key.png");
        Image eyeOpen = loadImage("/icon/eyeOpen.png");
        Image eyeClosed = loadImage("/icon/eyeClosed.png");

        // First Name
        IconTextField firstNameField = new IconTextField("First Name", userIcon);
        firstNameField.setFont(merriweatherRegular);
        card.add(firstNameField, "w 320!, h 40!");
        firstNameError = createErrorLabel();
        card.add(firstNameError, "w 320!, align left");

        // Last Name
        IconTextField lastNameField = new IconTextField("Last Name", userIcon);
        lastNameField.setFont(merriweatherRegular);
        card.add(lastNameField, "w 320!, h 40!");
        lastNameError = createErrorLabel();
        card.add(lastNameError, "w 320!, align left");

        // Email
        IconTextField emailField = new IconTextField("Email", emailIcon);
        emailField.setFont(merriweatherRegular);
        card.add(emailField, "w 320!, h 40!");
        emailError = createErrorLabel();
        card.add(emailError, "w 320!, align left");

        // Password
        TogglePasswordField passwordField =
                new TogglePasswordField("Password", passwordIcon, eyeOpen, eyeClosed);
        passwordField.setFont(merriweatherRegular);
        card.add(passwordField, "w 320!, h 40!");

        // Spacing between password and confirm password
        card.add(Box.createVerticalStrut(10));

        // Confirm Password
        TogglePasswordField confirmPasswordField =
                new TogglePasswordField("Confirm Password", passwordIcon, eyeOpen, eyeClosed);
        confirmPasswordField.setFont(merriweatherRegular);
        card.add(confirmPasswordField, "w 320!, h 40!");
        confirmPasswordError = createErrorLabel();
        card.add(confirmPasswordError, "w 320!, align left, wrap 15");

        // Sign Up button
        RoundedButton signUpBtn = new RoundedButton("Sign Up");
        signUpBtn.setFont(merriweatherBold.deriveFont(20f));
        signUpBtn.setBackground(new Color(33, 150, 243));
        signUpBtn.setForeground(Color.WHITE);
        card.add(signUpBtn, "w 320!, h 40!, wrap 15");

        // Realtime inline validation
        firstNameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                validateFirstName(firstNameField.getText().trim());
            }
        });

        lastNameField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                validateLastName(lastNameField.getText().trim());
            }
        });

        emailField.getDocument().addDocumentListener(new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                validateEmail(emailField.getText().trim());
            }
        });

        DocumentListener passwordListener = new SimpleDocumentListener() {
            @Override
            public void update(DocumentEvent e) {
                String password = new String(passwordField.getPassword());
                String confirmPassword = new String(confirmPasswordField.getPassword());
                validatePasswords(password, confirmPassword);
            }
        };
        passwordField.getDocument().addDocumentListener(passwordListener);
        confirmPasswordField.getDocument().addDocumentListener(passwordListener);

        // Signup logic (click)
        signUpBtn.addActionListener(e -> {
            clearErrors();

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            boolean valid = true;

            if (!validateFirstName(firstName)) {
                valid = false;
            }

            if (!validateLastName(lastName)) {
                valid = false;
            }

            if (!validateEmail(email)) {
                valid = false;
            }

            if (!validatePasswords(password, confirmPassword)) {
                valid = false;
            }

            if (!valid) return;

            signUpBtn.setEnabled(false);

            new Thread(() -> {
                try (Connection conn = DBConnection.connect()) {
                    String check = "SELECT 1 FROM users WHERE email = ?";
                    boolean emailExists;
                    try (PreparedStatement ps = conn.prepareStatement(check)) {
                        ps.setString(1, email);
                        try (ResultSet rs = ps.executeQuery()) {
                            emailExists = rs.next();
                        }
                    }

                    if (emailExists) {
                        SwingUtilities.invokeLater(() -> {
                            emailError.setText("Email is already registered");
                            emailError.setVisible(true);
                            signUpBtn.setEnabled(true);
                        });
                        return;
                    }

                    String insert =
                            "INSERT INTO users(first_name,last_name,email,password) VALUES(?,?,?,?)";
                    try (PreparedStatement ins = conn.prepareStatement(insert)) {
                        ins.setString(1, firstName);
                        ins.setString(2, lastName);
                        ins.setString(3, email);
                        ins.setString(4, password);
                        ins.executeUpdate();
                    }

                    SwingUtilities.invokeLater(() -> {
                        Window window = SwingUtilities.getWindowAncestor(signUpPage.this);
                        if (window instanceof windowManager) {
                            ((windowManager) window).showLogin();
                        }
                    });

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(
                                signUpPage.this,
                                "An error occurred while creating your account. Please try again.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        signUpBtn.setEnabled(true);
                    });
                }
            }).start();
        });

        // Footer: only "Sign in" is clickable
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);

        JLabel footerText = new JLabel("Already have an account?");
        footerText.setFont(merriweatherRegular.deriveFont(12f));

        JLabel footerLink = new JLabel(
                "<html><span style='color:#2196F3; text-decoration:underline;'>Sign in</span></html>");
        footerLink.setFont(merriweatherRegular.deriveFont(12f));
        footerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(signUpPage.this);
                if (window instanceof windowManager) {
                    ((windowManager) window).showLogin();
                }
            }
        });

        footerPanel.add(footerText);
        footerPanel.add(footerLink);
        card.add(footerPanel, "align center, wrap 0");
    }

    // ===== Helper methods =====
    private JLabel createErrorLabel() {
        JLabel label = new JLabel(" ");
        label.setFont(merriweatherRegular.deriveFont(11f));
        label.setForeground(new Color(220, 53, 69));
        label.setVisible(false);
        return label;
    }

    private void clearErrors() {
        firstNameError.setVisible(false);
        lastNameError.setVisible(false);
        emailError.setVisible(false);
        confirmPasswordError.setVisible(false);
    }

    private boolean validateFirstName(String firstName) {
        if (firstName.isEmpty()) {
            firstNameError.setText("First name is required");
            firstNameError.setVisible(true);
            return false;
        } else if (!firstName.matches("^[a-zA-Z]+(\\s[a-zA-Z]+)*$")) {
            firstNameError.setText("First name can only contain letters and spaces");
            firstNameError.setVisible(true);
            return false;
        } else {
            firstNameError.setVisible(false);
            return true;
        }
    }

    private boolean validateLastName(String lastName) {
        if (lastName.isEmpty()) {
            lastNameError.setText("Last name is required");
            lastNameError.setVisible(true);
            return false;
        } else if (!lastName.matches("^[a-zA-Z]+(\\s[a-zA-Z]+)*$")) {
            lastNameError.setText("Last name can only contain letters and spaces");
            lastNameError.setVisible(true);
            return false;
        } else {
            lastNameError.setVisible(false);
            return true;
        }
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            emailError.setVisible(true);
            return false;
        } else if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
            emailError.setText("Invalid email format");
            emailError.setVisible(true);
            return false;
        } else {
            emailError.setVisible(false);
            return true;
        }
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Passwords do not match");
            confirmPasswordError.setVisible(true);
            return false;
        } else {
            confirmPasswordError.setVisible(false);
            return true;
        }
    }

    private Font loadFont(String path, float size) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                throw new IllegalArgumentException("Font resource not found: " + path);
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return font.deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    private Image loadImage(String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            return new ImageIcon(url).getImage();
        }
        return null;
    }

    private abstract class SimpleDocumentListener implements DocumentListener {
        public abstract void update(DocumentEvent e);

        @Override
        public void insertUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update(e);
        }
    }

    // ================= ORIGINAL UI CLASSES =================
    class RoundedPanel extends JPanel {
        private int cornerRadius = 20;

        public RoundedPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            for (int i = 6; i > 0; i--) {
                float alpha = i * 0.05f;
                g2.setColor(new Color(255, 255, 255, (int)(alpha*255)));
                g2.setStroke(new BasicStroke(i));
                g2.drawRoundRect(i,i,w-i*2,h-i*2,cornerRadius,cornerRadius);
            }

            g2.setColor(new Color(255,255,255,120));
            g2.fillRoundRect(6,6,w-12,h-12,cornerRadius,cornerRadius);

            g2.setColor(new Color(255,255,255,200));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(6,6,w-12,h-12,cornerRadius,cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    class BackgroundPanel extends JPanel {
        private Image backgroundImage;

        public BackgroundPanel(String resourcePath) {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                backgroundImage = new ImageIcon(url).getImage();
            } else {
                backgroundImage = null;
            }
            setLayout(new MigLayout("fill, center"));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    class IconTextField extends JTextField {
        private String placeholder;
        private Image icon;
        private boolean focused;

        public IconTextField(String placeholder, Image icon) {
            this.placeholder = placeholder;
            this.icon = icon;
            setBorder(null);
            setOpaque(false);

            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent e) { focused=true; repaint(); }
                public void focusLost(FocusEvent e) { focused=false; repaint(); }
            });
        }

        public Insets getInsets() { return new Insets(5,40,5,10); }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255,255,255,200));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

            if(focused){
                g2.setColor(new Color(33,150,243,100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,20,20);
            }

            super.paintComponent(g2);

            g2.drawImage(icon,10,(getHeight()-20)/2,20,20,this);

            if(getText().isEmpty()){
                g2.setFont(merriweatherRegular);
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder,40,getHeight()/2+5);
            }

            g2.dispose();
        }
    }

    class TogglePasswordField extends JPasswordField {
        private String placeholder;
        private Image leftIcon, eyeOpen, eyeClosed;
        private boolean show;

        public TogglePasswordField(String placeholder, Image leftIcon, Image eyeOpen, Image eyeClosed){
            this.placeholder=placeholder; this.leftIcon=leftIcon; this.eyeOpen=eyeOpen; this.eyeClosed=eyeClosed;
            setBorder(null); setOpaque(false); setEchoChar('\u2022');

            addMouseListener(new MouseAdapter(){
                public void mousePressed(MouseEvent e){
                    if(e.getX()>=getWidth()-30){ show=!show; setEchoChar(show?(char)0:'\u2022'); repaint(); }
                }
            });
        }

        public Insets getInsets(){ return new Insets(5,40,5,40); }

        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(new Color(255,255,255,200));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

            super.paintComponent(g2);

            g2.drawImage(leftIcon,10,(getHeight()-20)/2,20,20,this);
            g2.drawImage(show?eyeOpen:eyeClosed,getWidth()-30,(getHeight()-20)/2,20,20,this);

            if(getPassword().length==0){
                g2.setFont(merriweatherRegular);
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder,40,getHeight()/2+5);
            }

            g2.dispose();
        }
    }

    class RoundedButton extends JButton {
        public RoundedButton(String text){
            super(text);
            setOpaque(false); setBorderPainted(false); setFocusPainted(false); setContentAreaFilled(false);
        }

        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg=getBackground();
            if(getModel().isPressed()) bg=bg.darker();
            else if(getModel().isRollover()) bg=bg.brighter();

            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),20,20);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            FontMetrics fm=g2.getFontMetrics();
            g2.drawString(getText(),(getWidth()-fm.stringWidth(getText()))/2,(getHeight()+fm.getAscent())/2-2);

            g2.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(signUpPage::new);
    }
}

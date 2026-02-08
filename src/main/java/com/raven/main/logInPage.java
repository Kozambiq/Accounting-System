package com.raven.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.miginfocom.swing.MigLayout;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class logInPage extends JPanel {

    private RoundedPanel card;

    // WorkSans fonts (Bold for bold, Regular for regular/italic/bold-italic)
    private Font workSansRegular;
    private Font workSansBold;

    public logInPage() {
        setLayout(new BorderLayout());

        // Load WorkSans fonts
        workSansRegular = loadFont("/fonts/Work_Sans/static/WorkSans-Regular.ttf", 14f);
        workSansBold = loadFont("/fonts/Work_Sans/static/WorkSans-Bold.ttf", 14f);

        // Background panel (same as signUpPage)
        BackgroundPanel backgroundPanel = new BackgroundPanel("/icon/bg3.jpg");
        add(backgroundPanel, BorderLayout.CENTER);

        // Rounded card (fixed width like signUpPage, height wraps contents)
        card = new RoundedPanel();
        card.setLayout(new MigLayout("wrap 1, gapy 4, align center, top 50"));
        backgroundPanel.add(card, "center, w 400!");

        // Logo at top (same logo and sizing as signUpPage)
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
        // smaller gap below logo so title sits closer
        card.add(logoLabel, "alignx center, wrap 0");

        // Title & subtitle
        JLabel title = new JLabel("Log In");
        title.setFont(workSansBold.deriveFont(36f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        // minimal extra gap below title; main spacing comes before subtitle
        card.add(title, "align center, span, wrap 0");

        JLabel subtitle = new JLabel("Enter your account credentials");
        subtitle.setFont(workSansRegular.deriveFont(14f));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(subtitle, "align center, span, wrap 20");

        // Load icons
        Image emailIcon = new ImageIcon(getClass().getResource("/icon/email.png")).getImage();
        Image passwordIcon = new ImageIcon(getClass().getResource("/icon/key.png")).getImage();
        Image eyeOpen = new ImageIcon(getClass().getResource("/icon/eyeOpen.png")).getImage();
        Image eyeClosed = new ImageIcon(getClass().getResource("/icon/eyeClosed.png")).getImage();

        // Email and password fields
        IconTextField emailField = new IconTextField("Email", emailIcon);
        emailField.setFont(workSansRegular.deriveFont(14f));
        card.add(emailField, "w 320!, h 40!, align center");

        TogglePasswordField passwordField = new TogglePasswordField("Password", passwordIcon, eyeOpen, eyeClosed);
        passwordField.setFont(workSansRegular.deriveFont(14f));
        card.add(passwordField, "w 320!, h 40!, align center, wrap 4");

        // Forgot Password link (right-aligned)
        JLabel forgotLink = new JLabel(
                "<html><span style='color:#2196F3; text-decoration:underline;'>Forgot Password</span></html>");
        forgotLink.setFont(workSansRegular.deriveFont(12f));
        forgotLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(logInPage.this);
                if (window instanceof windowManager) {
                    ((windowManager) window).showAccountRecovery();
                }
            }
        });
        card.add(forgotLink, "w 320!, align right, wrap 20");

        // Log in button
        RoundedButton loginBtn = new RoundedButton("Log In");
        loginBtn.setFont(workSansBold.deriveFont(20f));
        loginBtn.setBackground(new Color(33, 150, 243));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(loginBtn, "w 320!, h 40!, align center, wrap 20");

        // Login logic: validate credentials against the users table
        loginBtn.addActionListener(e -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        logInPage.this,
                        "Please enter both email and password.",
                        "Missing information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            loginBtn.setEnabled(false);

            new Thread(() -> {
                boolean success = false;
                int[] userIdHolder = new int[1];
                String[] nameHolder = new String[1];

                try (Connection conn = DBConnection.connect()) {
                    // Fetch the user's id and name parts so we can start a session.
                    String sql = "SELECT id, first_name, last_name FROM users WHERE email = ? AND password = ?";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, email);
                        ps.setString(2, password);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                success = true;
                                userIdHolder[0] = rs.getInt("id");
                                String firstName = rs.getString("first_name");
                                String lastName = rs.getString("last_name");
                                nameHolder[0] = (firstName + " " + lastName).trim();
                            }
                        }
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    final String message = "A database error occurred while logging in. Please try again.";
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(
                            logInPage.this,
                            message,
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    ));
                }

                final boolean loginOk = success;
                final int loggedInUserId = userIdHolder[0];
                final String loggedInName = nameHolder[0];

                SwingUtilities.invokeLater(() -> {
                    loginBtn.setEnabled(true);
                    if (loginOk) {
                        // Start in-memory session for this user
                        Session.start(loggedInUserId, loggedInName, email);

                        JOptionPane.showMessageDialog(
                                logInPage.this,
                                "Login successful.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        // After successful login, ask the windowManager to open the main dashboard
                        Window window = SwingUtilities.getWindowAncestor(logInPage.this);
                        if (window instanceof windowManager) {
                            ((windowManager) window).openHomePage();
                        }
                    } else {
                        JOptionPane.showMessageDialog(
                                logInPage.this,
                                "Invalid email or password.",
                                "Login failed",
                                JOptionPane.ERROR_MESSAGE
                        );
                    }
                });
            }).start();
        });

        // Footer: only "Sign Up" is clickable, same behavior style as signUpPage
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        footerPanel.setOpaque(false);

        JLabel footerText = new JLabel("Don't have an account?");
        footerText.setFont(workSansRegular.deriveFont(12f));

        JLabel footerLink = new JLabel(
                "<html><span style='color:#2196F3; text-decoration:underline;'>Sign Up</span></html>");
        footerLink.setFont(workSansRegular.deriveFont(12f));
        footerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footerLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(logInPage.this);
                if (window instanceof windowManager) {
                    ((windowManager) window).showSignup();
                }
            }
        });

        footerPanel.add(footerText);
        footerPanel.add(footerLink);
        card.add(footerPanel, "align center, wrap, gapbottom 30");

    }

    private Font loadFont(String path, float size) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    // RoundedPanel with glow
    class RoundedPanel extends JPanel {
        private int cornerRadius = 20;
        public RoundedPanel() { setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            // Soft glow
            for (int i = 6; i > 0; i--) {
                float alpha = i * 0.06f;
                g2.setColor(new Color(255, 255, 255, (int)(alpha*255)));
                g2.setStroke(new BasicStroke(i));
                g2.drawRoundRect(i, i, w - i*2, h - i*2, cornerRadius, cornerRadius);
            }

            // Transparent background
            g2.setColor(new Color(255,255,255,140));
            g2.fillRoundRect(6,6,w-12,h-12,cornerRadius,cornerRadius);

            // Crisp border
            g2.setColor(new Color(255,255,255,200));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(6,6,w-12,h-12,cornerRadius,cornerRadius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Background panel (stretched to fill parent)
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(String resourcePath) {
            backgroundImage = new ImageIcon(getClass().getResource(resourcePath)).getImage();
            setLayout(new MigLayout("fill, center","[grow]","[grow]"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Email field
    class IconTextField extends JTextField {
        private String placeholder;
        private Image icon;
        private int arc=20, iconSize=20, iconPadding=10;
        private boolean focused=false;

        public IconTextField(String placeholder, Image icon) {
            this.placeholder = placeholder;
            this.icon = icon;
            setBorder(null);
            setOpaque(false);
            setText("");
            setCaretColor(Color.BLACK);

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e){ focused=true; repaint();}
                public void focusLost(java.awt.event.FocusEvent e){ focused=false; repaint();}
            });
        }

        @Override
        public Insets getInsets() {
            Insets insets = super.getInsets();
            int left = iconSize + iconPadding + 5;
            return new Insets(insets.top, left, insets.bottom, insets.right);
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,200));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);

            if(focused){
                g2.setColor(new Color(33,150,243,100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,arc,arc);
            }

            super.paintComponent(g2);

            if(icon!=null) g2.drawImage(icon,10,(getHeight()-iconSize)/2,iconSize,iconSize,this);
            if(getText().isEmpty() && workSansRegular!=null){
                g2.setFont(workSansRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize+10+iconPadding, getHeight()/2 + g2.getFontMetrics().getAscent()/2 -2);
            }

            g2.dispose();
        }
    }

    // Password field
    class TogglePasswordField extends JPasswordField {
        private String placeholder;
        private Image leftIcon, eyeOpen, eyeClosed;
        private boolean showPassword=false;
        private int arc=20, iconSize=20, iconPadding=10;
        private boolean focused=false;

        public TogglePasswordField(String placeholder, Image leftIcon, Image eyeOpen, Image eyeClosed){
            this.placeholder = placeholder;
            this.leftIcon = leftIcon;
            this.eyeOpen = eyeOpen;
            this.eyeClosed = eyeClosed;
            setBorder(null);
            setOpaque(false);
            setEchoChar('\u2022');
            setCaretColor(Color.BLACK);

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e){ focused=true; repaint();}
                public void focusLost(java.awt.event.FocusEvent e){ focused=false; repaint();}
            });

            addMouseListener(new MouseAdapter(){
                @Override
                public void mousePressed(MouseEvent e){
                    int x = e.getX();
                    if(x>=getWidth()-iconSize-iconPadding){
                        showPassword = !showPassword;
                        setEchoChar(showPassword?(char)0:'\u2022');
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter(){
                @Override
                public void mouseMoved(MouseEvent e){
                    int x = e.getX();
                    if(x>=getWidth()-iconSize-iconPadding){
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    }else{
                        setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
                    }
                }
            });
        }

        @Override
        public Insets getInsets(){
            Insets insets = super.getInsets();
            int left = iconSize + iconPadding + 5;
            int right = iconSize + iconPadding + 5;
            return new Insets(insets.top,left,insets.bottom,right);
        }

        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,200));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);

            if(focused){
                g2.setColor(new Color(33,150,243,100));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(1,1,getWidth()-2,getHeight()-2,arc,arc);
            }

            super.paintComponent(g2);

            if(leftIcon!=null) g2.drawImage(leftIcon,10,(getHeight()-iconSize)/2,iconSize,iconSize,this);
            if(getPassword().length==0 && workSansRegular!=null){
                g2.setFont(workSansRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize+10+iconPadding,getHeight()/2 + g2.getFontMetrics().getAscent()/2 -2);
            }

            Image eyeIcon = showPassword?eyeOpen:eyeClosed;
            if(eyeIcon!=null) g2.drawImage(eyeIcon,getWidth()-iconSize-iconPadding,(getHeight()-iconSize)/2,iconSize,iconSize,this);

            g2.dispose();
        }
    }

    // Rounded button
    class RoundedButton extends JButton{
        private int arc = 20;
        public RoundedButton(String text){
            super(text);
            setOpaque(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setContentAreaFilled(false);
        }
        @Override
        protected void paintComponent(Graphics g){
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getBackground();
            if(getModel().isPressed()) bg = bg.darker();
            else if(getModel().isRollover()) bg = bg.brighter();

            g2.setColor(bg);
            g2.fillRoundRect(0,0,getWidth(),getHeight(),arc,arc);

            g2.setFont(getFont());
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            String text = getText();
            int x = (getWidth()-fm.stringWidth(text))/2;
            int y = (getHeight()+fm.getAscent())/2 -2;
            g2.drawString(text,x,y);

            g2.dispose();
        }
    }

}

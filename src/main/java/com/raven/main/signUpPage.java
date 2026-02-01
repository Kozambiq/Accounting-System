package com.raven.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.miginfocom.swing.MigLayout;
import java.io.InputStream;
import java.sql.*;
import java.util.regex.Pattern;

public class signUpPage extends JFrame {

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
        setTitle("Sign Up");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Load fonts
        merriweatherRegular = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Regular.ttf", 14f);
        merriweatherBold = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Bold.ttf", 14f);
        merriweatherItalic = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Italic.ttf", 14f);
        merriweatherBoldItalic = loadFont("/fonts/Merriweather/static/Merriweather_120pt-BoldItalic.ttf", 14f);

        // Background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel("/icon/bg3.jpg");
        setContentPane(backgroundPanel);

        // Card with top padding to push content up
        card = new RoundedPanel();
        card.setLayout(new MigLayout("wrap 1, gapy 4, align center, top 50"));
        card.setPreferredSize(new Dimension(400, 720));
        backgroundPanel.add(card, "center");

        // Logo (kept big)
        Image logoImg = new ImageIcon(getClass().getResource("/icon/logo.png")).getImage();
        ImageIcon logoIcon = new ImageIcon(logoImg.getScaledInstance(250, 180, Image.SCALE_SMOOTH));
        card.add(new JLabel(logoIcon), "wrap 10, pushy 0"); // pushy 0 keeps it from pushing other content down

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
        Image userIcon = new ImageIcon(getClass().getResource("/icon/user.png")).getImage();
        Image emailIcon = new ImageIcon(getClass().getResource("/icon/email.png")).getImage();
        Image passwordIcon = new ImageIcon(getClass().getResource("/icon/key.png")).getImage();
        Image eyeOpen = new ImageIcon(getClass().getResource("/icon/eyeOpen.png")).getImage();
        Image eyeClosed = new ImageIcon(getClass().getResource("/icon/eyeClosed.png")).getImage();

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

        // Signup logic
        signUpBtn.addActionListener(e -> {
            clearErrors();

            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            boolean valid = true;

            if (!firstName.matches("[a-zA-Z]+")) {
                firstNameError.setText("First name can only contain letters");
                firstNameError.setVisible(true);
                valid = false;
            }

            if (!lastName.matches("[a-zA-Z]+")) {
                lastNameError.setText("Last name can only contain letters");
                lastNameError.setVisible(true);
                valid = false;
            }

            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", email)) {
                emailError.setText("Invalid email format");
                emailError.setVisible(true);
                valid = false;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordError.setText("Passwords do not match");
                confirmPasswordError.setVisible(true);
                valid = false;
            }

            if (!valid) return;

            try (Connection conn = DBconnection.connect()) {
                String check = "SELECT 1 FROM users WHERE email = ?";
                PreparedStatement ps = conn.prepareStatement(check);
                ps.setString(1, email);

                if (ps.executeQuery().next()) {
                    emailError.setText("Email is already registered");
                    emailError.setVisible(true);
                    return;
                }

                String insert =
                        "INSERT INTO users(first_name,last_name,email,password) VALUES(?,?,?,?)";
                PreparedStatement ins = conn.prepareStatement(insert);
                ins.setString(1, firstName);
                ins.setString(2, lastName);
                ins.setString(3, email);
                ins.setString(4, password);
                ins.executeUpdate();

                new logInPage().setVisible(true);
                dispose();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Footer
        JLabel footer = new JLabel(
                "<html>Already have an account? <span style='color:#2196F3; text-decoration:underline;'>Sign in</span></html>");
        footer.setFont(merriweatherRegular.deriveFont(12f));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        footer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                new logInPage().setVisible(true);
                dispose();
            }
        });
        card.add(footer, "align center, wrap 0");

        setVisible(true);
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

    private Font loadFont(String path, float size) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return font.deriveFont(size);
        } catch (Exception e) {
            return new Font("Arial", Font.PLAIN, (int) size);
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
            backgroundImage = new ImageIcon(getClass().getResource(resourcePath)).getImage();
            setLayout(new MigLayout("fill, center"));
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
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

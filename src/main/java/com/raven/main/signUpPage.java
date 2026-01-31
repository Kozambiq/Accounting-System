package com.raven.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.miginfocom.swing.MigLayout;
import java.io.InputStream;

public class signUpPage extends JFrame {

    private RoundedPanel card;

    // Merriweather fonts
    private Font merriweatherRegular;
    private Font merriweatherBold;
    private Font merriweatherItalic;
    private Font merriweatherBoldItalic;

    public signUpPage() {
        setTitle("Sign Up");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Load all Merriweather fonts from resources
        merriweatherRegular = loadFont("/fonts/Merriweather/static/Merriweather_120pt-Regular.ttf", 14f);
        merriweatherBold = loadFont("/fonts/Merriweather/Merriweather-Bold.ttf", 14f);
        merriweatherItalic = loadFont("/fonts/Merriweather/Merriweather-Italic.ttf", 14f);
        merriweatherBoldItalic = loadFont("/fonts/Merriweather/Merriweather-BoldItalic.ttf", 14f);

        // Background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel("/icon/background.jpg");
        setContentPane(backgroundPanel);

        // Rounded card
        card = new RoundedPanel();
        card.setLayout(new MigLayout("wrap 1, gapy 15, align center", "[grow]", "[]"));
        card.setPreferredSize(new Dimension(400, 700));
        backgroundPanel.add(card, "center");

        // Logo at top (smaller and spaced)
        Image logoImg = new ImageIcon(getClass().getResource("/icon/background-logo.png")).getImage();
        ImageIcon logoIcon = new ImageIcon(logoImg.getScaledInstance(250, 120, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, "align center, span, wrap 10"); // 10px space below

        // Title & subtitle
        JLabel title = new JLabel("Sign Up");
        title.setFont(merriweatherBold.deriveFont(36f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "align center, span, wrap 5");

        JLabel subtitle = new JLabel("Create your account");
        subtitle.setFont(merriweatherRegular.deriveFont(14f));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(subtitle, "align center, span, wrap 20"); // 20px space below subtitle

        // Load PNG icons from resources
        Image userIcon = new ImageIcon(getClass().getResource("/icon/user.png")).getImage();
        Image emailIcon = new ImageIcon(getClass().getResource("/icon/email.png")).getImage();
        Image passwordIcon = new ImageIcon(getClass().getResource("/icon/key.png")).getImage();
        Image eyeOpen = new ImageIcon(getClass().getResource("/icon/eyeOpen.png")).getImage();
        Image eyeClosed = new ImageIcon(getClass().getResource("/icon/eyeClosed.png")).getImage();

        // Input fields
        IconTextField firstNameField = new IconTextField("First Name", userIcon);
        firstNameField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(firstNameField, "w 320!, h 40!, align center");

        IconTextField lastNameField = new IconTextField("Last Name", userIcon);
        lastNameField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(lastNameField, "w 320!, h 40!, align center");

        IconTextField emailField = new IconTextField("Email", emailIcon);
        emailField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(emailField, "w 320!, h 40!, align center");

        TogglePasswordField passwordField = new TogglePasswordField("Password", passwordIcon, eyeOpen, eyeClosed);
        passwordField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(passwordField, "w 320!, h 40!, align center");

        TogglePasswordField confirmPasswordField = new TogglePasswordField("Confirm Password", passwordIcon, eyeOpen, eyeClosed);
        confirmPasswordField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(confirmPasswordField, "w 320!, h 40!, align center, wrap 20"); // space before button

        // Sign-up button
        RoundedButton signUpBtn = new RoundedButton("Sign Up");
        signUpBtn.setFont(merriweatherBold.deriveFont(20f));
        signUpBtn.setBackground(new Color(33, 150, 243));
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(signUpBtn, "w 320!, h 40!, align center, wrap 20"); // space before footer

        // Footer with clickable "Sign in"
        JLabel footer = new JLabel();
        footer.setFont(merriweatherRegular.deriveFont(12f));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        // Only "Sign in" clickable and styled
        footer.setText("<html>Already have an account? <span style='color:#2196F3; text-decoration:underline;'>Sign in</span></html>");
        // Change cursor on hover for the clickable part
        footer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // approximate position of "Sign in"
                footer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        footer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Sign in clicked!");
                // TODO: open login page here
            }
        });
        card.add(footer, "align center, span");

        setVisible(true);
    }

    private Font loadFont(String path, float size) {
        try {
            InputStream stream = getClass().getResourceAsStream(path);
            Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.PLAIN, (int) size); // fallback
        }
    }

    // RoundedPanel
    class RoundedPanel extends JPanel {
        private int cornerRadius = 20;
        public RoundedPanel() { setOpaque(false); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255,255,255,140));
            g2.fillRoundRect(0,0,getWidth(),getHeight(),cornerRadius,cornerRadius);
            g2.setColor(new Color(200,200,200,120));
            g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,cornerRadius,cornerRadius);
            super.paintComponent(g);
            g2.dispose();
        }
    }

    // BackgroundPanel
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(String resourcePath) {
            backgroundImage = new ImageIcon(getClass().getResource(resourcePath)).getImage();
            setLayout(new MigLayout("fill, center","[grow]","[grow]"));
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(backgroundImage,0,0,getWidth(),getHeight(),this);
        }
    }

    // IconTextField
    class IconTextField extends JTextField {
        private String placeholder;
        private Image icon;
        private int arc = 20, iconSize = 20, iconPadding = 10;
        private boolean focused = false;

        public IconTextField(String placeholder, Image icon) {
            this.placeholder = placeholder;
            this.icon = icon;
            setBorder(null);
            setOpaque(false);
            setText("");
            setCaretColor(Color.BLACK);

            addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent e) { focused = true; repaint(); }
                public void focusLost(java.awt.event.FocusEvent e) { focused = false; repaint(); }
            });
        }

        @Override
        public Insets getInsets() {
            Insets insets = super.getInsets();
            int left = iconSize + iconPadding + 5;
            return new Insets(insets.top, left, insets.bottom, insets.right);
        }

        @Override
        protected void paintComponent(Graphics g) {
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

            if(icon!=null){
                g2.drawImage(icon,10,(getHeight()-iconSize)/2,iconSize,iconSize,this);
            }

            if(getText().isEmpty() && merriweatherRegular!=null){
                g2.setFont(merriweatherRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize+10+iconPadding, getHeight()/2 + g2.getFontMetrics().getAscent()/2 - 2);
            }

            g2.dispose();
        }
    }

    // TogglePasswordField
    class TogglePasswordField extends JPasswordField {
        private String placeholder;
        private Image leftIcon, eyeOpen, eyeClosed;
        private boolean showPassword = false;
        private int arc = 20, iconSize=20, iconPadding=10;
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
                    if(x >= getWidth()-iconSize-iconPadding){
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
            if(getPassword().length==0 && merriweatherRegular!=null){
                g2.setFont(merriweatherRegular.deriveFont(14f));
                g2.setColor(Color.GRAY);
                g2.drawString(placeholder, iconSize+10+iconPadding,getHeight()/2 + g2.getFontMetrics().getAscent()/2 -2);
            }

            Image eyeIcon = showPassword?eyeOpen:eyeClosed;
            if(eyeIcon!=null) g2.drawImage(eyeIcon,getWidth()-iconSize-iconPadding,(getHeight()-iconSize)/2,iconSize,iconSize,this);

            g2.dispose();
        }
    }

    // RoundedButton
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(signUpPage::new);
    }
}

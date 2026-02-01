package com.raven.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import net.miginfocom.swing.MigLayout;
import java.io.InputStream;

public class logInPage extends JFrame {

    private RoundedPanel card;

    // Merriweather fonts
    private Font merriweatherRegular;
    private Font merriweatherBold;
    private Font merriweatherItalic;
    private Font merriweatherBoldItalic;

    public logInPage() {
        setTitle("Log In");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Load Merriweather fonts
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
        card.setPreferredSize(new Dimension(400, 500));
        backgroundPanel.add(card, "center");

        // Logo at top
        Image logoImg = new ImageIcon(getClass().getResource("/icon/background-logo.png")).getImage();
        ImageIcon logoIcon = new ImageIcon(logoImg.getScaledInstance(250, 120, Image.SCALE_SMOOTH));
        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(logoLabel, "align center, span, wrap 10");

        // Title & subtitle
        JLabel title = new JLabel("Log In");
        title.setFont(merriweatherBold.deriveFont(36f));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(title, "align center, span, wrap 5");

        JLabel subtitle = new JLabel("Enter your account credentials");
        subtitle.setFont(merriweatherRegular.deriveFont(14f));
        subtitle.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(subtitle, "align center, span, wrap 20");

        // Load icons
        Image emailIcon = new ImageIcon(getClass().getResource("/icon/email.png")).getImage();
        Image passwordIcon = new ImageIcon(getClass().getResource("/icon/key.png")).getImage();
        Image eyeOpen = new ImageIcon(getClass().getResource("/icon/eyeOpen.png")).getImage();
        Image eyeClosed = new ImageIcon(getClass().getResource("/icon/eyeClosed.png")).getImage();

        // Email and password fields
        IconTextField emailField = new IconTextField("Email", emailIcon);
        emailField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(emailField, "w 320!, h 40!, align center");

        TogglePasswordField passwordField = new TogglePasswordField("Password", passwordIcon, eyeOpen, eyeClosed);
        passwordField.setFont(merriweatherRegular.deriveFont(14f));
        card.add(passwordField, "w 320!, h 40!, align center, wrap 20");

        // Log in button
        RoundedButton loginBtn = new RoundedButton("Log In");
        loginBtn.setFont(merriweatherBold.deriveFont(20f));
        loginBtn.setBackground(new Color(33, 150, 243));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.add(loginBtn, "w 320!, h 40!, align center, wrap 20");

        // Footer with clickable "Sign Up"
        JLabel footer = new JLabel();
        footer.setFont(merriweatherRegular.deriveFont(12f));
        footer.setHorizontalAlignment(SwingConstants.CENTER);
        footer.setText("<html>Don't have an account? <span style='color:#2196F3; text-decoration:underline;'>Sign Up</span></html>");
        footer.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                footer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        footer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Sign Up clicked!");
                // TODO: open sign-up page here
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

    // Background panel (centered, fit image)
    class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        public BackgroundPanel(String resourcePath) {
            backgroundImage = new ImageIcon(getClass().getResource(resourcePath)).getImage();
            setLayout(new MigLayout("fill, center","[grow]","[grow]"));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int panelWidth = getWidth();
            int panelHeight = getHeight();
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);

            float widthRatio = (float) panelWidth / imgWidth;
            float heightRatio = (float) panelHeight / imgHeight;
            float ratio = Math.min(widthRatio, heightRatio);

            int drawWidth = (int) (imgWidth * ratio);
            int drawHeight = (int) (imgHeight * ratio);
            int x = (panelWidth - drawWidth)/2;
            int y = (panelHeight - drawHeight)/2;

            g.drawImage(backgroundImage, x, y, drawWidth, drawHeight, this);
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
            if(getText().isEmpty() && merriweatherRegular!=null){
                g2.setFont(merriweatherRegular.deriveFont(14f));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(logInPage::new);
    }
}

package com.smartpos;

import javax.swing.*;
import java.awt.*;


public class SplashScreen extends JWindow {

    private JLabel loadingLabel;
    private JProgressBar progressBar;
    private int progress = 0;

    public SplashScreen() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setSize(screenBounds.width, screenBounds.height);
        setLocation(screenBounds.x, screenBounds.y);

        JPanel root = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2.setPaint(new GradientPaint(0, 0, new Color(0x1B, 0x26, 0x3B), 0, getHeight(), new Color(0x0D, 0x1B, 0x2A)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setBackground(new Color(0x1B, 0x26, 0x3B));
        setContentPane(root);

        int winW = screenBounds.width;
        int winH = screenBounds.height;

        
        JLabel titleLabel = new JLabel("SMART SUPERMARKET", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(0, winH / 2 - 120, winW, 60);
        root.add(titleLabel);

        
        JLabel subtitleLabel = new JLabel("POS & SELF-CHECKOUT SYSTEM", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        subtitleLabel.setForeground(new Color(0x2E, 0xCC, 0x71));
        subtitleLabel.setBounds(0, winH / 2 - 50, winW, 40);
        root.add(subtitleLabel);

       
        loadingLabel = new JLabel("Initializing...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        loadingLabel.setForeground(Color.LIGHT_GRAY);
        loadingLabel.setBounds(0, winH / 2 + 50, winW, 30);
        root.add(loadingLabel);

        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0x2E, 0xCC, 0x71));
        progressBar.setBackground(new Color(25, 30, 42));
        progressBar.setBorderPainted(false);
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        progressBar.setBounds(winW / 2 - 300, winH / 2 + 100, 600, 25);
        root.add(progressBar);

        
        JLabel authorLabel = new JLabel("Created by Om Nathwani", SwingConstants.CENTER);
        authorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        authorLabel.setForeground(new Color(160, 165, 175));
        authorLabel.setBounds(0, winH - 50, winW, 30);
        root.add(authorLabel);
    }

    private void setUndecorated(boolean b) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setUndecorated'");
    }

    public void showAndLoad() {
        setVisible(true);
        Timer timer = new Timer(30, e -> {
            progress += 1;
            progressBar.setValue(progress);

            if (progress < 30) {
                loadingLabel.setText("Connecting Database...");
            } else if (progress < 60) {
                loadingLabel.setText("Loading Inventory...");
            } else if (progress < 90) {
                loadingLabel.setText("Loading Products...");
            } else {
                loadingLabel.setText("Ready!");
            }

            if (progress >= 100) {
                ((Timer) e.getSource()).stop();
                
                
                Timer closeTimer = new Timer(500, ev -> {
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        try {
                            new LoginFrame().setVisible(true);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });
                });
                closeTimer.setRepeats(false);
                closeTimer.start();
            }
        });
        timer.start();
    }
}

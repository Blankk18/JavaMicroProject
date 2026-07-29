package com.smartpos;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("Starting Smart Supermarket POS & Self-Checkout...");
        System.out.println("==========================================");


        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set Look and Feel: " + e.getMessage());
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            try {

                SplashScreen splash = new SplashScreen();
                splash.showAndLoad();
            } catch (Exception e) {
                System.err.println("Error initializing Splash Screen:");
                e.printStackTrace();

                new LoginFrame().setVisible(true);
            }
        });
    }
}

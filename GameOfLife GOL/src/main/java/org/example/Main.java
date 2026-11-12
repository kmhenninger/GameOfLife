package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        // Sanfte Schriftglättung und moderne Fensterdekorationen
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        System.setProperty("flatlaf.useWindowDecorations", "true");

        SwingUtilities.invokeLater(() -> {
            try {
                // Standardmäßig Dark; im UI kann später umgeschaltet werden
                UIManager.setLookAndFeel(new FlatDarkLaf());
                FlatLaf.updateUI();
            } catch (Exception ignored) {
            }
            GameOfLifeFrame frame = new GameOfLifeFrame();
            frame.setVisible(true);
        });
    }
}
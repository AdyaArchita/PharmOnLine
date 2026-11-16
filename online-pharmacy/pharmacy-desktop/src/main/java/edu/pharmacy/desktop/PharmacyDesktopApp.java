package edu.pharmacy.desktop;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Entry point for the pharmacist and administrator desktop application. */
public final class PharmacyDesktopApp {
    private PharmacyDesktopApp() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
                System.err.println("Using the default Swing look and feel: " + exception.getMessage());
            }
            new DesktopFrame().setVisible(true);
        });
    }
}

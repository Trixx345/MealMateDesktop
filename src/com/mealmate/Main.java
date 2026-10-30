
package com.mealmate;

import com.mealmate.ui.LoginFrame;
import com.mealmate.util.AppLogger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        AppLogger.info("Starting MealMate");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ex) {
                AppLogger.warning(
                    "Could not apply system appearance: "
                    + ex.getMessage()
                );
            }

            new LoginFrame().setVisible(true);
        });
    }
}

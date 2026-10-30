
package com.mealmate.util;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class AppLogger {

    private static final Logger LOGGER =
        Logger.getLogger("com.mealmate");

    private AppLogger() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warning(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message, Throwable error) {
        LOGGER.log(Level.SEVERE, message, error);
    }
}

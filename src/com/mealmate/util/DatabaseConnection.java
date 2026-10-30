
package com.mealmate.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnection {

    private static final Properties CONFIG = loadConfig();

    private DatabaseConnection() {
    }

    private static Properties loadConfig() {
        Properties properties = new Properties();
        Path path = Path.of("config.properties");

        try {
            if (!Files.exists(path)) {
                throw new IllegalStateException(
                    "config.properties was not found. "
                    + "Run MealMate from its project folder."
                );
            }

            try (InputStream input = Files.newInputStream(path)) {
                properties.load(input);
            }

            return properties;

        } catch (IOException ex) {
            throw new ExceptionInInitializerError(
                "Could not read config.properties: "
                + ex.getMessage()
            );
        }
    }

    public static Connection getConnection()
            throws SQLException {

        String url = required("db.url");
        String username = required("db.user");
        String password = CONFIG.getProperty("db.password", "");

        return DriverManager.getConnection(
            url, username, password
        );
    }

    private static String required(String key) {
        String value = CONFIG.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                "Missing " + key + " in config.properties"
            );
        }

        return value.trim();
    }
}

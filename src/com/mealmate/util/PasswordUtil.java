
package com.mealmate.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 128;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                "Password is required."
            );
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] derived = derive(password.toCharArray(), salt);

        return "P2$"
            + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(salt)
            + "$"
            + HexFormat.of().formatHex(derived);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null || stored.isBlank()) {
            return false;
        }

        stored = stored.trim();

        // New PBKDF2 password format
        if (stored.startsWith("P2$")) {
            String[] parts = stored.split("\\$", -1);

            if (parts.length != 3) {
                return false;
            }

            try {
                byte[] salt =
                    Base64.getUrlDecoder().decode(parts[1]);

                byte[] expected =
                    HexFormat.of().parseHex(parts[2]);

                byte[] actual = derive(
                    password.toCharArray(), salt
                );

                return MessageDigest.isEqual(expected, actual);

            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        // Compatibility with the existing SHA-256 demo accounts
        if (stored.matches("(?i)[0-9a-f]{64}")) {
            try {
                byte[] actual = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                        password.getBytes(StandardCharsets.UTF_8)
                    );

                byte[] expected = HexFormat.of().parseHex(stored);

                return MessageDigest.isEqual(expected, actual);

            } catch (Exception ex) {
                return false;
            }
        }

        return false;
    }

    public static boolean isLegacySha256(String stored) {
        return stored != null
            && stored.trim().matches("(?i)[0-9a-f]{64}");
    }

    private static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(
            password, salt, ITERATIONS, KEY_BITS
        );

        try {
            return SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA256")
                .generateSecret(spec)
                .getEncoded();

        } catch (Exception ex) {
            throw new IllegalStateException(
                "Password hashing failed.", ex
            );

        } finally {
            spec.clearPassword();
            Arrays.fill(password, '\0');
        }
    }
}

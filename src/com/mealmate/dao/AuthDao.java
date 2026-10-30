package com.mealmate.dao;

import com.mealmate.model.AppUser;
import com.mealmate.util.AppLogger;
import com.mealmate.util.DatabaseConnection;
import com.mealmate.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

public class AuthDao {

    private static final Pattern USERNAME_PATTERN =
        Pattern.compile("[A-Za-z0-9._-]{4,40}");

    public AppUser authenticate(
            String username, String password) throws SQLException {

        String sql = """
            SELECT user_id, username, password_hash,
                   user_role, student_id, active
            FROM mbs_users
            WHERE username = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, username.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !result.getBoolean("active")) {
                    return null;
                }

                int userId = result.getInt("user_id");
                String storedHash = result.getString("password_hash");
                String role = result.getString("user_role");
                int rawStudentId = result.getInt("student_id");
                Integer studentId =
                    result.wasNull() ? null : rawStudentId;

                if (!PasswordUtil.verify(password, storedHash)) {
                    return null;
                }

                if ("STUDENT".equalsIgnoreCase(role)
                        && studentId == null) {
                    throw new SQLException(
                        "The student account is not linked to a student."
                    );
                }

                if (PasswordUtil.isLegacySha256(storedHash)) {
                    String updatedHash = PasswordUtil.hash(password);

                    try (PreparedStatement update =
                             connection.prepareStatement("""
                                 UPDATE mbs_users
                                 SET password_hash = ?
                                 WHERE user_id = ?
                                 """)) {
                        update.setString(1, updatedHash);
                        update.setInt(2, userId);
                        update.executeUpdate();
                    }

                    AppLogger.info(
                        "Password hash upgraded for " + username
                    );
                }

                return new AppUser(
                    userId,
                    result.getString("username"),
                    role.toUpperCase(),
                    studentId
                );
            }
        }
    }

    // Admin creates a login or resets credentials for an existing student.
    public void createStudentLogin(
            int studentId,
            String username,
            String temporaryPassword) throws SQLException {

        validateUsername(username);
        validatePassword(temporaryPassword);

        username = username.trim();

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                try (PreparedStatement statement =
                         connection.prepareStatement("""
                             SELECT student_id
                             FROM mbs_students
                             WHERE student_id = ?
                             FOR UPDATE
                             """)) {

                    statement.setInt(1, studentId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (studentId <= 0 || !result.next()) {
                            throw new IllegalArgumentException(
                                "Select a valid student."
                            );
                        }
                    }
                }

                Integer existingUserId = null;

                try (PreparedStatement statement =
                         connection.prepareStatement("""
                             SELECT user_id
                             FROM mbs_users
                             WHERE student_id = ?
                             FOR UPDATE
                             """)) {

                    statement.setInt(1, studentId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            existingUserId = result.getInt("user_id");
                        }
                    }
                }

                checkUsernameAvailable(
                    connection, username, existingUserId
                );

                String hash = PasswordUtil.hash(temporaryPassword);

                if (existingUserId != null) {
                    try (PreparedStatement statement =
                             connection.prepareStatement("""
                                 UPDATE mbs_users
                                 SET username = ?,
                                     password_hash = ?,
                                     user_role = 'STUDENT',
                                     active = TRUE
                                 WHERE user_id = ?
                                   AND student_id = ?
                                 """)) {

                        statement.setString(1, username);
                        statement.setString(2, hash);
                        statement.setInt(3, existingUserId);
                        statement.setInt(4, studentId);

                        if (statement.executeUpdate() != 1) {
                            throw new SQLException(
                                "Could not update student credentials."
                            );
                        }
                    }
                } else {
                    try (PreparedStatement statement =
                             connection.prepareStatement("""
                                 INSERT INTO mbs_users
                                     (username, password_hash, user_role,
                                      student_id, active)
                                 VALUES (?, ?, 'STUDENT', ?, TRUE)
                                 """)) {

                        statement.setString(1, username);
                        statement.setString(2, hash);
                        statement.setInt(3, studentId);
                        statement.executeUpdate();
                    }
                }

                connection.commit();
                AppLogger.info(
                    "Student login credentials saved for student ID "
                    + studentId
                );

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw translateDuplicate(ex);

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    // Student changes their own username and password.
    public void changeOwnCredentials(
            int userId,
            String currentPassword,
            String newUsername,
            String newPassword) throws SQLException {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                "Invalid user account."
            );
        }

        validateUsername(newUsername);
        validatePassword(newPassword);
        newUsername = newUsername.trim();

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                String storedHash;
                String role;
                boolean active;
                int studentId;

                try (PreparedStatement statement =
                         connection.prepareStatement("""
                             SELECT password_hash, user_role,
                                    active, student_id
                             FROM mbs_users
                             WHERE user_id = ?
                             FOR UPDATE
                             """)) {

                    statement.setInt(1, userId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                "Your user account was not found."
                            );
                        }

                        storedHash = result.getString("password_hash");
                        role = result.getString("user_role");
                        active = result.getBoolean("active");
                        studentId = result.getInt("student_id");

                        if (result.wasNull()) {
                            throw new IllegalArgumentException(
                                "This account is not linked to a student."
                            );
                        }
                    }
                }

                if (!active || !"STUDENT".equalsIgnoreCase(role)) {
                    throw new IllegalArgumentException(
                        "Only an active student account can use this page."
                    );
                }

                if (currentPassword == null
                        || !PasswordUtil.verify(
                            currentPassword, storedHash
                        )) {
                    throw new IllegalArgumentException(
                        "Your current password is incorrect."
                    );
                }

                // Confirm this student's linked record still exists.
                try (PreparedStatement statement =
                         connection.prepareStatement("""
                             SELECT student_id
                             FROM mbs_students
                             WHERE student_id = ?
                             """)) {

                    statement.setInt(1, studentId);

                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new IllegalArgumentException(
                                "Your student record could not be found."
                            );
                        }
                    }
                }

                checkUsernameAvailable(
                    connection, newUsername, userId
                );

                String newHash = PasswordUtil.hash(newPassword);

                try (PreparedStatement statement =
                         connection.prepareStatement("""
                             UPDATE mbs_users
                             SET username = ?,
                                 password_hash = ?
                             WHERE user_id = ?
                               AND student_id = ?
                               AND user_role = 'STUDENT'
                               AND active = TRUE
                             """)) {

                    statement.setString(1, newUsername);
                    statement.setString(2, newHash);
                    statement.setInt(3, userId);
                    statement.setInt(4, studentId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                            "Your credentials could not be updated."
                        );
                    }
                }

                connection.commit();

                AppLogger.info(
                    "Student credentials updated for user ID "
                    + userId
                );

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw translateDuplicate(ex);

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    public void register(
            String rollNo,
            String fullName,
            String email,
            String phone,
            String password) throws SQLException {

        String studentSql = """
            INSERT INTO mbs_students
                (roll_no, full_name, email, phone, wallet_balance)
            VALUES (?, ?, ?, ?, 0.00)
            """;

        String userSql = """
            INSERT INTO mbs_users
                (username, password_hash, user_role,
                 student_id, active)
            VALUES (?, ?, 'STUDENT', ?, TRUE)
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                int studentId;

                try (PreparedStatement statement =
                         connection.prepareStatement(
                             studentSql,
                             Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, rollNo.trim());
                    statement.setString(2, fullName.trim());
                    statement.setString(3, blankToNull(email));
                    statement.setString(4, blankToNull(phone));
                    statement.executeUpdate();

                    try (ResultSet keys =
                             statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException(
                                "Could not create the student record."
                            );
                        }

                        studentId = keys.getInt(1);
                    }
                }

                try (PreparedStatement statement =
                         connection.prepareStatement(userSql)) {

                    statement.setString(1, rollNo.trim());
                    statement.setString(2, PasswordUtil.hash(password));
                    statement.setInt(3, studentId);
                    statement.executeUpdate();
                }

                connection.commit();
                AppLogger.info("Registered student: " + rollNo.trim());

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw ex;

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    private void checkUsernameAvailable(
            Connection connection,
            String username,
            Integer currentUserId) throws SQLException {

        String sql = """
            SELECT user_id
            FROM mbs_users
            WHERE username = ?
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int foundId = result.getInt("user_id");

                    if (currentUserId == null
                            || foundId != currentUserId) {
                        throw new IllegalArgumentException(
                            "That username is already in use."
                        );
                    }
                }
            }
        }
    }

    private void validateUsername(String username) {
        if (username == null
                || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new IllegalArgumentException(
                "Username must be 4-40 characters using letters, "
                + "numbers, dots, underscores or hyphens."
            );
        }
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < 8
                || password.length() > 128) {
            throw new IllegalArgumentException(
                "Password must be 8-128 characters."
            );
        }
    }

    private void rollback(
            Connection connection, Exception original) {
        try {
            connection.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private void restoreAutoCommit(Connection connection) {
        try {
            connection.setAutoCommit(true);
        } catch (SQLException ex) {
            AppLogger.warning(
                "Could not restore auto-commit: " + ex.getMessage()
            );
        }
    }

    private SQLException translateDuplicate(Exception ex) {
        if (ex instanceof SQLException sqlException
                && sqlException.getErrorCode() == 1062) {
            return new SQLException(
                "That username is already in use.",
                sqlException
            );
        }

        if (ex instanceof SQLException sqlException) {
            return sqlException;
        }

        if (ex instanceof RuntimeException runtimeException) {
            throw runtimeException;
        }

        return new SQLException(ex);
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty()
            ? null
            : value.trim();
    }
}
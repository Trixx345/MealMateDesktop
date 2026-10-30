
package com.mealmate.dao;

import com.mealmate.model.Student;
import com.mealmate.util.AppLogger;
import com.mealmate.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StudentDao {

    public Student findById(int studentId) throws SQLException {
        String sql = """
            SELECT student_id, roll_no, full_name, email,
                   phone, wallet_balance
            FROM mbs_students
            WHERE student_id = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public Student findByUsername(
            String username) throws SQLException {

        String sql = """
            SELECT s.student_id, s.roll_no, s.full_name,
                   s.email, s.phone, s.wallet_balance
            FROM mbs_students s
            JOIN mbs_users u ON u.student_id = s.student_id
            WHERE u.username = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<Student> findAll() throws SQLException {
        String sql = """
            SELECT student_id, roll_no, full_name, email,
                   phone, wallet_balance
            FROM mbs_students
            ORDER BY student_id DESC
            """;

        List<Student> students = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                students.add(map(result));
            }
        }

        return students;
    }

    public void updateProfile(
            int studentId,
            String fullName,
            String email,
            String phone) throws SQLException {

        String sql = """
            UPDATE mbs_students
            SET full_name = ?, email = ?, phone = ?
            WHERE student_id = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, fullName.trim());
            statement.setString(2, blankToNull(email));
            statement.setString(3, blankToNull(phone));
            statement.setInt(4, studentId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException(
                    "Student profile was not found."
                );
            }

            AppLogger.info(
                "Updated profile for student ID " + studentId
            );
        }
    }

    public void creditWallet(
            int studentId, BigDecimal amount) throws SQLException {

        if (amount == null
                || amount.signum() <= 0
                || amount.scale() > 2) {
            throw new IllegalArgumentException(
                "Enter a positive amount with up to two decimal places."
            );
        }

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                String updateSql = """
                    UPDATE mbs_students
                    SET wallet_balance = wallet_balance + ?
                    WHERE student_id = ?
                    """;

                try (PreparedStatement statement =
                         connection.prepareStatement(updateSql)) {

                    statement.setBigDecimal(1, amount);
                    statement.setInt(2, studentId);

                    if (statement.executeUpdate() != 1) {
                        throw new SQLException(
                            "Student record was not found."
                        );
                    }
                }

                // The database migration permits a NULL booking_id
                // for wallet top-ups.
                String paymentSql = """
                    INSERT INTO mbs_payments
                        (booking_id, student_id, amount,
                         payment_type, payment_status, note)
                    VALUES (NULL, ?, ?, 'WALLET_TOPUP',
                            'SUCCESS', ?)
                    """;

                try (PreparedStatement statement =
                         connection.prepareStatement(paymentSql)) {

                    statement.setInt(1, studentId);
                    statement.setBigDecimal(2, amount);
                    statement.setString(
                        3, "Administrative demo wallet credit"
                    );
                    statement.executeUpdate();
                }

                connection.commit();

                AppLogger.info(
                    "Wallet credited for student " + studentId
                );

            } catch (SQLException | RuntimeException ex) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackError) {
                    ex.addSuppressed(rollbackError);
                }
                throw ex;

            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException ex) {
                    AppLogger.warning(
                        "Could not restore auto-commit: "
                        + ex.getMessage()
                    );
                }
            }
        }
    }

    private Student map(ResultSet result) throws SQLException {
        return new Student(
            result.getInt("student_id"),
            result.getString("roll_no"),
            result.getString("full_name"),
            result.getString("email"),
            result.getString("phone"),
            result.getBigDecimal("wallet_balance")
        );
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty()
            ? null
            : value.trim();
    }
}


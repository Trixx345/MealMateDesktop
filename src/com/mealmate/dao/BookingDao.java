
package com.mealmate.dao;

import com.mealmate.model.Booking;
import com.mealmate.util.AppLogger;
import com.mealmate.util.DatabaseConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    public List<Booking> findAll() throws SQLException {
        String sql = """
            SELECT b.booking_id, b.student_id, s.roll_no,
                   s.full_name, b.meal_id, m.meal_name,
                   b.quantity, b.total_amount, b.booking_status,
                   b.booked_at, b.cancelled_at
            FROM mbs_bookings b
            JOIN mbs_students s ON s.student_id = b.student_id
            JOIN mbs_meals m ON m.meal_id = b.meal_id
            ORDER BY b.booking_id DESC
            """;

        List<Booking> bookings = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                bookings.add(map(result));
            }
        }

        return bookings;
    }

    public List<Booking> findForStudent(
            int studentId) throws SQLException {

        String sql = """
            SELECT b.booking_id, b.student_id, s.roll_no,
                   s.full_name, b.meal_id, m.meal_name,
                   b.quantity, b.total_amount, b.booking_status,
                   b.booked_at, b.cancelled_at
            FROM mbs_bookings b
            JOIN mbs_students s ON s.student_id = b.student_id
            JOIN mbs_meals m ON m.meal_id = b.meal_id
            WHERE b.student_id = ?
            ORDER BY b.booking_id DESC
            """;

        List<Booking> bookings = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    bookings.add(map(result));
                }
            }
        }

        return bookings;
    }

    public int create(
            int studentId,
            int mealId,
            int quantity) throws SQLException {

        return create(
            studentId,
            mealId,
            quantity,
            LocalDate.now(),
            "Lunch",
            ""
        );
    }

    public int create(
            int studentId,
            int mealId,
            int quantity,
            LocalDate bookingDate,
            String timeSlot,
            String instructions) throws SQLException {

        validateQuantity(quantity);

        if (bookingDate == null) {
            throw new IllegalArgumentException(
                "Select a booking date."
            );
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Booking date cannot be in the past."
            );
        }

        if (timeSlot == null
                || !List.of(
                    "Breakfast", "Lunch", "Snacks", "Dinner"
                ).contains(timeSlot)) {
            throw new IllegalArgumentException(
                "Choose a valid time slot."
            );
        }

        String notes = instructions == null ? "" : instructions.trim();

        if (notes.length() > 500) {
            throw new IllegalArgumentException(
                "Special instructions cannot exceed 500 characters."
            );
        }

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BigDecimal wallet = getLockedWallet(
                    connection, studentId
                );

                MealData meal = getLockedMeal(
                    connection, mealId
                );

                if (meal.stock() < quantity) {
                    throw new SQLException(
                        "Not enough stock. Available quantity: "
                        + meal.stock()
                    );
                }

                BigDecimal total = meal.price()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

                if (wallet.compareTo(total) < 0) {
                    throw new SQLException(
                        "Insufficient wallet balance. Booking cost: ₹"
                        + total + ". Available: ₹" + wallet
                    );
                }

                update(connection, """
                    UPDATE mbs_students
                    SET wallet_balance = wallet_balance - ?
                    WHERE student_id = ?
                      AND wallet_balance >= ?
                    """, total, studentId, total);

                update(connection, """
                    UPDATE mbs_meals
                    SET available_qty = available_qty - ?
                    WHERE meal_id = ?
                      AND available_qty >= ?
                    """, quantity, mealId, quantity);

                int bookingId;

                String insertBooking = """
                    INSERT INTO mbs_bookings
                        (student_id, meal_id, quantity,
                         total_amount, booking_status, booking_date,
                         time_slot, special_instructions)
                    VALUES (?, ?, ?, ?, 'BOOKED', ?, ?, ?)
                    """;

                try (PreparedStatement statement =
                         connection.prepareStatement(
                             insertBooking,
                             Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, studentId);
                    statement.setInt(2, mealId);
                    statement.setInt(3, quantity);
                    statement.setBigDecimal(4, total);
                    statement.setDate(
                        5, java.sql.Date.valueOf(bookingDate)
                    );
                    statement.setString(6, timeSlot);
                    statement.setString(
                        7, notes.isEmpty() ? null : notes
                    );

                    statement.executeUpdate();

                    try (ResultSet keys =
                             statement.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                "Could not obtain the booking ID."
                            );
                        }

                        bookingId = keys.getInt(1);
                    }
                }

                insertPayment(
                    connection,
                    bookingId,
                    studentId,
                    total,
                    "BOOKING",
                    "Meal booking payment"
                );

                connection.commit();

                AppLogger.info(
                    "Created booking " + bookingId
                );

                return bookingId;

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw ex;

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    public void cancel(
            int bookingId,
            Integer ownerStudentId) throws SQLException {

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BookingData booking = getLockedBooking(
                    connection, bookingId, ownerStudentId
                );

                if (!"BOOKED".equals(booking.status())) {
                    throw new SQLException(
                        "This booking has already been cancelled."
                    );
                }

                // Lock the student and meal records before changing
                // their balances and inventory.
                getLockedWallet(connection, booking.studentId());

                getLockedMeal(connection, booking.mealId());

                update(connection, """
                    UPDATE mbs_bookings
                    SET booking_status = 'CANCELLED',
                        cancelled_at = CURRENT_TIMESTAMP
                    WHERE booking_id = ?
                      AND booking_status = 'BOOKED'
                    """, bookingId);

                update(connection, """
                    UPDATE mbs_students
                    SET wallet_balance = wallet_balance + ?
                    WHERE student_id = ?
                    """, booking.total(), booking.studentId());

                update(connection, """
                    UPDATE mbs_meals
                    SET available_qty = available_qty + ?
                    WHERE meal_id = ?
                    """, booking.quantity(), booking.mealId());

                insertPayment(
                    connection,
                    bookingId,
                    booking.studentId(),
                    booking.total(),
                    "REFUND",
                    "Booking cancellation refund"
                );

                connection.commit();

                AppLogger.info(
                    "Cancelled booking " + bookingId
                );

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw ex;

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    public void changeQuantity(
            int bookingId,
            Integer ownerStudentId,
            int newQuantity) throws SQLException {

        validateQuantity(newQuantity);

        try (Connection connection =
                 DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                BookingData booking = getLockedBooking(
                    connection, bookingId, ownerStudentId
                );

                if (!"BOOKED".equals(booking.status())) {
                    throw new SQLException(
                        "Cancelled bookings cannot be modified."
                    );
                }

                if (newQuantity == booking.quantity()) {
                    connection.rollback();
                    return;
                }

                BigDecimal wallet = getLockedWallet(
                    connection, booking.studentId()
                );

                MealData meal = getLockedMeal(
                    connection, booking.mealId()
                );

                int differenceInQuantity =
                    newQuantity - booking.quantity();

                if (differenceInQuantity > 0
                        && meal.stock() < differenceInQuantity) {
                    throw new SQLException(
                        "Not enough additional stock. Available: "
                        + meal.stock()
                    );
                }

                BigDecimal unitPrice = booking.total()
                    .divide(
                        BigDecimal.valueOf(booking.quantity()),
                        4,
                        RoundingMode.HALF_UP
                    );

                BigDecimal newTotal = unitPrice
                    .multiply(BigDecimal.valueOf(newQuantity))
                    .setScale(2, RoundingMode.HALF_UP);

                BigDecimal differenceInMoney = newTotal
                    .subtract(booking.total())
                    .setScale(2, RoundingMode.HALF_UP);

                if (differenceInMoney.signum() > 0
                        && wallet.compareTo(differenceInMoney) < 0) {
                    throw new SQLException(
                        "Insufficient wallet balance for the increase."
                    );
                }

                if (differenceInMoney.signum() > 0) {
                    update(connection, """
                        UPDATE mbs_students
                        SET wallet_balance = wallet_balance - ?
                        WHERE student_id = ?
                          AND wallet_balance >= ?
                        """,
                        differenceInMoney,
                        booking.studentId(),
                        differenceInMoney
                    );

                } else if (differenceInMoney.signum() < 0) {
                    update(connection, """
                        UPDATE mbs_students
                        SET wallet_balance = wallet_balance + ?
                        WHERE student_id = ?
                        """,
                        differenceInMoney.abs(),
                        booking.studentId()
                    );
                }

                update(connection, """
                    UPDATE mbs_meals
                    SET available_qty = available_qty - ?
                    WHERE meal_id = ?
                    """,
                    differenceInQuantity,
                    booking.mealId()
                );

                update(connection, """
                    UPDATE mbs_bookings
                    SET quantity = ?, total_amount = ?
                    WHERE booking_id = ?
                      AND booking_status = 'BOOKED'
                    """,
                    newQuantity,
                    newTotal,
                    bookingId
                );

                if (differenceInMoney.signum() > 0) {
                    insertPayment(
                        connection,
                        bookingId,
                        booking.studentId(),
                        differenceInMoney,
                        "BOOKING",
                        "Additional charge after quantity change"
                    );

                } else if (differenceInMoney.signum() < 0) {
                    insertPayment(
                        connection,
                        bookingId,
                        booking.studentId(),
                        differenceInMoney.abs(),
                        "REFUND",
                        "Refund after quantity change"
                    );
                }

                connection.commit();

                AppLogger.info(
                    "Changed booking " + bookingId
                    + " quantity to " + newQuantity
                );

            } catch (SQLException | RuntimeException ex) {
                rollback(connection, ex);
                throw ex;

            } finally {
                restoreAutoCommit(connection);
            }
        }
    }

    private BigDecimal getLockedWallet(
            Connection connection,
            int studentId) throws SQLException {

        String sql = """
            SELECT wallet_balance
            FROM mbs_students
            WHERE student_id = ?
            FOR UPDATE
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                        "Student record was not found."
                    );
                }

                return result.getBigDecimal("wallet_balance");
            }
        }
    }

    private MealData getLockedMeal(
            Connection connection,
            int mealId) throws SQLException {

        String sql = """
            SELECT price, available_qty
            FROM mbs_meals
            WHERE meal_id = ? AND active = TRUE
            FOR UPDATE
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, mealId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                        "Meal was not found or is inactive."
                    );
                }

                return new MealData(
                    result.getBigDecimal("price"),
                    result.getInt("available_qty")
                );
            }
        }
    }

    private BookingData getLockedBooking(
            Connection connection,
            int bookingId,
            Integer ownerStudentId) throws SQLException {

        String sql = """
            SELECT student_id, meal_id, quantity,
                   total_amount, booking_status
            FROM mbs_bookings
            WHERE booking_id = ?
            """ + (ownerStudentId == null
                ? ""
                : " AND student_id = ?")
              + " FOR UPDATE";

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);

            if (ownerStudentId != null) {
                statement.setInt(2, ownerStudentId);
            }

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                        "Booking not found or not owned by this student."
                    );
                }

                return new BookingData(
                    result.getInt("student_id"),
                    result.getInt("meal_id"),
                    result.getInt("quantity"),
                    result.getBigDecimal("total_amount"),
                    result.getString("booking_status")
                );
            }
        }
    }

    private void insertPayment(
            Connection connection,
            int bookingId,
            int studentId,
            BigDecimal amount,
            String type,
            String note) throws SQLException {

        String sql = """
            INSERT INTO mbs_payments
                (booking_id, student_id, amount, payment_type,
                 payment_status, note)
            VALUES (?, ?, ?, ?, 'SUCCESS', ?)
            """;

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, bookingId);
            statement.setInt(2, studentId);
            statement.setBigDecimal(3, amount);
            statement.setString(4, type);
            statement.setString(5, note);
            statement.executeUpdate();
        }
    }

    private void update(
            Connection connection,
            String sql,
            Object... values) throws SQLException {

        try (PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }

            int changed = statement.executeUpdate();

            if (changed != 1) {
                throw new SQLException(
                    "A required database record was not updated."
                );
            }
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException(
                "Quantity must be between 1 and 100."
            );
        }
    }

    private void rollback(
            Connection connection,
            Exception original) {

        try {
            connection.rollback();
        } catch (SQLException ex) {
            original.addSuppressed(ex);
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

    private Booking map(ResultSet result) throws SQLException {
        Timestamp bookedAt = result.getTimestamp("booked_at");
        Timestamp cancelledAt =
            result.getTimestamp("cancelled_at");

        return new Booking(
            result.getInt("booking_id"),
            result.getInt("student_id"),
            result.getString("roll_no"),
            result.getString("full_name"),
            result.getInt("meal_id"),
            result.getString("meal_name"),
            result.getInt("quantity"),
            result.getBigDecimal("total_amount"),
            result.getString("booking_status"),
            bookedAt,
            cancelledAt
        );
    }

    private record MealData(
        BigDecimal price,
        int stock
    ) {}

    private record BookingData(
        int studentId,
        int mealId,
        int quantity,
        BigDecimal total,
        String status
    ) {}


    public void markCollected(int bookingId) throws SQLException {
    if (bookingId <= 0) {
        throw new IllegalArgumentException("Invalid booking ID.");
    }

    String sql = """
        UPDATE mbs_bookings
        SET booking_status = 'COLLECTED',
            collected_at = CURRENT_TIMESTAMP
        WHERE booking_id = ?
          AND booking_status = 'BOOKED'
        """;

    try (Connection connection =
             DatabaseConnection.getConnection();
         PreparedStatement statement =
             connection.prepareStatement(sql)) {

        statement.setInt(1, bookingId);

        if (statement.executeUpdate() != 1) {
            throw new SQLException(
                "Booking not found, already cancelled, "
                + "or already collected."
            );
        }

        AppLogger.info(
            "Booking marked as collected: " + bookingId
        );
    }
}
}

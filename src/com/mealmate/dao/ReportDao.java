
package com.mealmate.dao;

import com.mealmate.model.ReportData;
import com.mealmate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    private static final String[] REPORT_NAMES = {
        "Daily Booking Summary",
        "Popular Meals",
        "Payment Ledger",
        "Student Wallet Balances",
        "Booking History",
        "Meal Inventory"
    };

    // Fixed SQL statements: report selection is by index,
    // never by concatenating user-provided SQL.
    private static final String[] REPORT_SQL = {
        """
        SELECT booking_date AS booking_date,
               COUNT(*) AS booking_count,
               SUM(total_amount) AS booking_value
        FROM mbs_bookings
        WHERE booking_status = 'BOOKED'
        GROUP BY booking_date
        ORDER BY booking_date DESC
        """,

        """
        SELECT m.meal_name,
               SUM(b.quantity) AS units_booked,
               SUM(b.total_amount) AS sales_amount
        FROM mbs_bookings b
        JOIN mbs_meals m ON m.meal_id = b.meal_id
        WHERE b.booking_status = 'BOOKED'
        GROUP BY m.meal_id, m.meal_name
        ORDER BY units_booked DESC, m.meal_name
        """,

        """
        SELECT payment_id, booking_id, student_id,
               amount, payment_type, payment_status,
               payment_time, note
        FROM mbs_payments
        ORDER BY payment_time DESC, payment_id DESC
        """,

        """
        SELECT roll_no, full_name, email,
               wallet_balance, created_at
        FROM mbs_students
        ORDER BY wallet_balance DESC, full_name
        """,

        """
        SELECT b.booking_id, s.roll_no, s.full_name,
               m.meal_name, b.quantity, b.total_amount,
               b.booking_status, b.booking_date,
               b.time_slot, b.special_instructions,
               b.booked_at, b.cancelled_at
        FROM mbs_bookings b
        JOIN mbs_students s ON s.student_id = b.student_id
        JOIN mbs_meals m ON m.meal_id = b.meal_id
        ORDER BY b.booking_id DESC
        """,

        """
        SELECT meal_id, meal_name, category, food_type,
               price, available_qty, active
        FROM mbs_meals
        ORDER BY active DESC, category, meal_name
        """
    };

    public String[] getReportNames() {
        return REPORT_NAMES.clone();
    }

    public ReportData getReport(int reportIndex)
            throws SQLException {

        if (reportIndex < 0
                || reportIndex >= REPORT_SQL.length) {
            throw new IllegalArgumentException(
                "Please select a valid report."
            );
        }

        List<String> columns = new ArrayList<>();
        List<List<Object>> rows = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(
                     REPORT_SQL[reportIndex]
                 );
             ResultSet result = statement.executeQuery()) {

            ResultSetMetaData metadata =
                result.getMetaData();

            for (int i = 1;
                 i <= metadata.getColumnCount();
                 i++) {
                columns.add(metadata.getColumnLabel(i));
            }

            while (result.next()) {
                List<Object> row = new ArrayList<>();

                for (int i = 1;
                     i <= metadata.getColumnCount();
                     i++) {
                    row.add(result.getObject(i));
                }

                rows.add(row);
            }
        }

        return new ReportData(
            List.copyOf(columns),
            List.copyOf(
                rows.stream().map(List::copyOf).toList()
            )
        );
    }
}


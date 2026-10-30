package com.mealmate.ui;

import com.mealmate.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

public final class BookingReceiptDialog {

    private BookingReceiptDialog() {
    }

    // Show the most recent booking for a student.
    public static void showLatestForStudent(
            Component parent, int studentId) {

        String sql =
            "SELECT booking_id FROM mbs_bookings " +
            "WHERE student_id = ? " +
            "ORDER BY booking_id DESC LIMIT 1";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, studentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    showForBooking(
                        parent,
                        rs.getInt("booking_id")
                    );
                } else {
                    JOptionPane.showMessageDialog(
                        parent,
                        "No booking was found.",
                        "MealMate Receipt",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }

        } catch (SQLException ex) {
            showError(parent, ex);
        }
    }

    // Show the receipt for one exact booking ID.
    public static void showForBooking(
            Component parent, int bookingId) {

        String sql =
            "SELECT b.booking_id, b.pickup_code, " +
            "s.roll_no, s.full_name, s.email, " +
            "m.meal_name, b.quantity, b.total_amount, " +
            "b.booking_date, b.time_slot, " +
            "b.special_instructions, b.booking_status, " +
            "b.booked_at " +
            "FROM mbs_bookings b " +
            "JOIN mbs_students s ON s.student_id = b.student_id " +
            "JOIN mbs_meals m ON m.meal_id = b.meal_id " +
            "WHERE b.booking_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    JOptionPane.showMessageDialog(
                        parent,
                        "Booking #" + bookingId + " was not found.",
                        "MealMate Receipt",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                String code = rs.getString("pickup_code");
                String bookingDate =
                    rs.getDate("booking_date") == null
                        ? "Not specified"
                        : rs.getDate("booking_date").toString();

                String timeSlot = valueOrDefault(
                    rs.getString("time_slot"),
                    "Not specified"
                );

                String instructions = valueOrDefault(
                    rs.getString("special_instructions"),
                    "None"
                );

                Timestamp timestamp = rs.getTimestamp("booked_at");

                String bookedAt = timestamp == null
                    ? "Not available"
                    : timestamp.toLocalDateTime().format(
                        DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm a"
                        )
                    );

                String receipt =
                    "====================================\n" +
                    "          MEALMATE RECEIPT           \n" +
                    "====================================\n\n" +
                    "BOOKING #" + rs.getInt("booking_id") + "\n\n" +
                    "PICKUP CODE\n" +
                    valueOrDefault(code, "Not generated") + "\n\n" +
                    "------------------------------------\n" +
                    "Student      : " +
                    valueOrDefault(rs.getString("full_name"), "") + "\n" +
                    "Roll Number  : " +
                    valueOrDefault(rs.getString("roll_no"), "") + "\n" +
                    "Email        : " +
                    valueOrDefault(rs.getString("email"), "") + "\n\n" +
                    "Meal         : " + rs.getString("meal_name") + "\n" +
                    "Quantity     : " + rs.getInt("quantity") + "\n" +
                    "Total        : Rs. " +
                    rs.getBigDecimal("total_amount") + "\n" +
                    "Booking Date : " + bookingDate + "\n" +
                    "Time Slot    : " + timeSlot + "\n" +
                    "Booked At    : " + bookedAt + "\n" +
                    "Instructions : " + instructions + "\n\n" +
                    "Status       : " +
                    rs.getString("booking_status") + "\n" +
                    "------------------------------------\n\n" +
                    "Show your pickup code at the counter.\n" +
                    "Collection is confirmed separately by admin.\n";

                showReceiptWindow(parent, receipt);
            }

        } catch (SQLException ex) {
            showError(parent, ex);
        }
    }

    private static void showReceiptWindow(
            Component parent, String receipt) {

        Window owner = parent == null
            ? null
            : SwingUtilities.getWindowAncestor(parent);

        JDialog dialog = new JDialog(
            owner,
            "MealMate | Digital Receipt",
            Dialog.ModalityType.APPLICATION_MODAL
        );

        JTextArea text = new JTextArea(receipt);
        text.setEditable(false);
        text.setFont(new Font("Monospaced", Font.PLAIN, 13));
        text.setBackground(Color.WHITE);
        text.setBorder(new EmptyBorder(18, 18, 18, 18));
        text.setCaretPosition(0);

        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));
        root.add(new JScrollPane(text), BorderLayout.CENTER);

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        JButton print = new JButton("Print / Save as PDF");
        JButton close = new JButton("Close");

        print.addActionListener(event -> {
            try {
                text.print();
            } catch (java.awt.print.PrinterException ex) {
                showError(dialog, ex);
            }
        });

        close.addActionListener(
            event -> dialog.dispose()
        );

        buttons.add(print);
        buttons.add(close);

        root.add(buttons, BorderLayout.SOUTH);

        dialog.setContentPane(root);
        dialog.setSize(520, 620);
        dialog.setMinimumSize(new Dimension(440, 450));
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
    }

    private static String valueOrDefault(
            String value, String fallback) {

        return value == null || value.trim().isEmpty()
            ? fallback
            : value;
    }

    private static void showError(
            Component parent, Exception ex) {

        JOptionPane.showMessageDialog(
            parent,
            "Could not load the receipt:\n" + ex.getMessage(),
            "MealMate Receipt Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
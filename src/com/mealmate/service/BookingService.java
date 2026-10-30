
package com.mealmate.service;

import com.mealmate.dao.BookingDao;
import com.mealmate.model.Booking;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BookingService {

    private final BookingDao bookingDao = new BookingDao();

    public List<Booking> allBookings() throws SQLException {
        return bookingDao.findAll();
    }

    public List<Booking> studentBookings(
            int studentId) throws SQLException {

        if (studentId <= 0) {
            throw new IllegalArgumentException(
                "Invalid student account."
            );
        }

        return bookingDao.findForStudent(studentId);
    }

    public int create(
            int studentId,
            int mealId,
            int quantity) throws SQLException {

        return bookingDao.create(
            studentId, mealId, quantity
        );
    }

    public int create(
            int studentId,
            int mealId,
            int quantity,
            LocalDate bookingDate,
            String timeSlot,
            String instructions) throws SQLException {

        if (studentId <= 0 || mealId <= 0) {
            throw new IllegalArgumentException(
                "A valid student and meal must be selected."
            );
        }

        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException(
                "Quantity must be between 1 and 100."
            );
        }

        if (bookingDate == null) {
            throw new IllegalArgumentException(
                "Choose a booking date."
            );
        }

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Booking date cannot be in the past."
            );
        }

        return bookingDao.create(
            studentId,
            mealId,
            quantity,
            bookingDate,
            timeSlot,
            instructions
        );
    }

    public void cancel(
            int bookingId,
            Integer ownerStudentId) throws SQLException {

        if (bookingId <= 0) {
            throw new IllegalArgumentException(
                "Invalid booking selection."
            );
        }

        if (ownerStudentId != null && ownerStudentId <= 0) {
            throw new IllegalArgumentException(
                "Invalid student account."
            );
        }

        bookingDao.cancel(
            bookingId, ownerStudentId
        );
    }

    public void changeQuantity(
            int bookingId,
            Integer ownerStudentId,
            int newQuantity) throws SQLException {

        if (bookingId <= 0) {
            throw new IllegalArgumentException(
                "Invalid booking selection."
            );
        }

        if (newQuantity < 1 || newQuantity > 100) {
            throw new IllegalArgumentException(
                "Quantity must be between 1 and 100."
            );
        }

        bookingDao.changeQuantity(
            bookingId,
            ownerStudentId,
            newQuantity
               );
    }

    public void markCollected(int bookingId) throws SQLException {
        if (bookingId <= 0) {
            throw new IllegalArgumentException(
                "Select a valid booking."
            );
        }

        bookingDao.markCollected(bookingId);
    }
}
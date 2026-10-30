
package com.mealmate.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record Booking(
    int id,
    int studentId,
    String rollNo,
    String studentName,
    int mealId,
    String mealName,
    int quantity,
    BigDecimal totalAmount,
    String status,
    Timestamp bookedAt,
    Timestamp cancelledAt
) {}

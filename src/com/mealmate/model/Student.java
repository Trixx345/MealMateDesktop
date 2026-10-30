
package com.mealmate.model;

import java.math.BigDecimal;

public record Student(
    int id,
    String rollNo,
    String fullName,
    String email,
    String phone,
    BigDecimal walletBalance
) {}

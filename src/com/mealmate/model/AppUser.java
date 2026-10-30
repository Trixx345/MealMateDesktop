
package com.mealmate.model;

public record AppUser(
    int userId,
    String username,
    String role,
    Integer studentId
) {
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public boolean isStudent() {
        return "STUDENT".equalsIgnoreCase(role);
    }
}

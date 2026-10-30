
package com.mealmate.service;

import com.mealmate.dao.AuthDao;
import com.mealmate.model.AppUser;

import java.sql.SQLException;
import java.util.regex.Pattern;

public class AuthService {

    private final AuthDao authDao = new AuthDao();

    private static final Pattern ROLL_NUMBER =
        Pattern.compile("[A-Za-z0-9_-]{4,30}");

    private static final Pattern EMAIL =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public AppUser login(String username, String password)
            throws SQLException {

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Enter your username."
            );
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                "Enter your password."
            );
        }

        AppUser user = authDao.authenticate(
            username.trim(), password
        );

        if (user == null) {
            throw new IllegalArgumentException(
                "Incorrect username or password, or the account is inactive."
            );
        }

        return user;
    }

    public void register(
            String rollNo,
            String fullName,
            String email,
            String phone,
            String password,
            String confirmPassword) throws SQLException {

        rollNo = clean(rollNo);
        fullName = clean(fullName);
        email = clean(email);
        phone = clean(phone);

        if (!ROLL_NUMBER.matcher(rollNo).matches()) {
            throw new IllegalArgumentException(
                "Roll number must be 4-30 characters and may contain "
                + "letters, numbers, hyphens or underscores."
            );
        }

        if (fullName.length() < 2 || fullName.length() > 100) {
            throw new IllegalArgumentException(
                "Full name must be between 2 and 100 characters."
            );
        }

        if (!email.isEmpty()
                && (!EMAIL.matcher(email).matches()
                || email.length() > 120)) {
            throw new IllegalArgumentException(
                "Enter a valid email address of at most 120 characters."
            );
        }

        if (phone.length() > 20) {
            throw new IllegalArgumentException(
                "Phone number cannot exceed 20 characters."
            );
        }

        if (password == null
                || password.length() < 8
                || password.length() > 128) {
            throw new IllegalArgumentException(
                "Password must contain between 8 and 128 characters."
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new IllegalArgumentException(
                "The passwords do not match."
            );
        }

        authDao.register(
            rollNo,
            fullName,
            email,
            phone,
            password
        );
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}



package com.mealmate.dao;

import com.mealmate.model.Meal;
import com.mealmate.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MealDao {

    public List<Meal> findActive(
            String search,
            String category,
            String foodType,
            BigDecimal maxPrice,
            String sort) throws SQLException {

        String sql = """
            SELECT meal_id, meal_name, category, description,
                   price, available_qty, active
            FROM mbs_meals
            WHERE active = TRUE
              AND (? = '' OR meal_name LIKE ?
                   OR category LIKE ?
                   OR COALESCE(description, '') LIKE ?)
              AND (? = '' OR category = ?)
              AND (? = '' OR food_type = ?)
              AND (? = TRUE OR price <= ?)
            """ + orderBy(sort);

        String term = search == null ? "" : search.trim();
        String pattern = "%" + term + "%";

        String chosenCategory =
            category == null
            || category.isBlank()
            || category.equals("All categories")
                ? "" : category.trim();

        String chosenFoodType =
            foodType == null
            || foodType.isBlank()
            || foodType.equals("All")
                ? "" : foodType.trim();

        boolean hasMaxPrice = maxPrice != null;

        BigDecimal safeMax = hasMaxPrice
            ? maxPrice
            : new BigDecimal("999999999.99");

        List<Meal> meals = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, term);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setString(4, pattern);

            statement.setString(5, chosenCategory);
            statement.setString(6, chosenCategory);

            statement.setString(7, chosenFoodType);
            statement.setString(8, chosenFoodType);

            statement.setBoolean(9, !hasMaxPrice);
            statement.setBigDecimal(10, safeMax);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    meals.add(map(result));
                }
            }
        }

        return meals;
    }

    public List<Meal> findAllForAdmin() throws SQLException {
        String sql = """
            SELECT meal_id, meal_name, category, description,
                   price, available_qty, active
            FROM mbs_meals
            ORDER BY active DESC, category ASC, meal_name ASC
            """;

        List<Meal> meals = new ArrayList<>();

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                meals.add(map(result));
            }
        }

        return meals;
    }

    public Meal findById(int mealId) throws SQLException {
        String sql = """
            SELECT meal_id, meal_name, category, description,
                   price, available_qty, active
            FROM mbs_meals
            WHERE meal_id = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, mealId);

            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? map(result) : null;
            }
        }
    }

    public List<String> findCategories() throws SQLException {
        String sql = """
            SELECT DISTINCT category
            FROM mbs_meals
            WHERE active = TRUE
            ORDER BY category
            """;

        List<String> categories = new ArrayList<>();
        categories.add("All categories");

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                categories.add(result.getString("category"));
            }
        }

        return categories;
    }

    public void create(
            String name,
            String category,
            String foodType,
            String description,
            BigDecimal price,
            int quantity) throws SQLException {

        String sql = """
            INSERT INTO mbs_meals
                (meal_name, category, food_type, description,
                 price, available_qty, active)
            VALUES (?, ?, ?, ?, ?, ?, TRUE)
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());
            statement.setString(2, category);
            statement.setString(3, foodType);
            statement.setString(4, blankToNull(description));
            statement.setBigDecimal(5, price);
            statement.setInt(6, quantity);

            statement.executeUpdate();
        }
    }

    public void update(
            int mealId,
            String name,
            String category,
            String foodType,
            String description,
            BigDecimal price,
            int quantity,
            boolean active) throws SQLException {

        String sql = """
            UPDATE mbs_meals
            SET meal_name = ?,
                category = ?,
                food_type = ?,
                description = ?,
                price = ?,
                available_qty = ?,
                active = ?
            WHERE meal_id = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());
            statement.setString(2, category);
            statement.setString(3, foodType);
            statement.setString(4, blankToNull(description));
            statement.setBigDecimal(5, price);
            statement.setInt(6, quantity);
            statement.setBoolean(7, active);
            statement.setInt(8, mealId);

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Meal was not found.");
            }
        }
    }

    public void deactivate(int mealId) throws SQLException {
        String sql = """
            UPDATE mbs_meals
            SET active = FALSE
            WHERE meal_id = ?
            """;

        try (Connection connection =
                 DatabaseConnection.getConnection();
             PreparedStatement statement =
                 connection.prepareStatement(sql)) {

            statement.setInt(1, mealId);
            statement.executeUpdate();
        }
    }

    private String orderBy(String sort) {
        if ("price-asc".equals(sort)) {
            return " ORDER BY price ASC, meal_name ASC";
        }

        if ("price-desc".equals(sort)) {
            return " ORDER BY price DESC, meal_name ASC";
        }

        if ("name".equals(sort)) {
            return " ORDER BY meal_name ASC";
        }

        return " ORDER BY category ASC, meal_name ASC";
    }

    private Meal map(ResultSet result) throws SQLException {
        return new Meal(
            result.getInt("meal_id"),
            result.getString("meal_name"),
            result.getString("category"),
            result.getString("description"),
            result.getBigDecimal("price"),
            result.getInt("available_qty"),
            result.getBoolean("active")
        );
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty()
            ? null : value.trim();
    }
}

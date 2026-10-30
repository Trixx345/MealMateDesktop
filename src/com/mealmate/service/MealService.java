
package com.mealmate.service;

import com.mealmate.dao.MealDao;
import com.mealmate.model.Meal;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class MealService {

    private final MealDao mealDao = new MealDao();

    public List<Meal> search(
            String search,
            String category,
            String foodType,
            String maxPriceText,
            String sort) throws SQLException {

        BigDecimal maxPrice = null;

        if (maxPriceText != null
                && !maxPriceText.trim().isEmpty()) {
            try {
                maxPrice = new BigDecimal(maxPriceText.trim());
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                    "Maximum price must be a valid number."
                );
            }

            if (maxPrice.signum() < 0 || maxPrice.scale() > 2) {
                throw new IllegalArgumentException(
                    "Maximum price must be non-negative "
                    + "with up to two decimal places."
                );
            }
        }

        return mealDao.findActive(
            search,
            category,
            foodType,
            maxPrice,
            sort
        );
    }

    public Meal findById(int mealId) throws SQLException {
        if (mealId <= 0) {
            throw new IllegalArgumentException(
                "Invalid meal selection."
            );
        }

        return mealDao.findById(mealId);
    }

    public List<String> categories() throws SQLException {
        return mealDao.findCategories();
    }

    public List<Meal> allForAdmin() throws SQLException {
        return mealDao.findAllForAdmin();
    }

    public void create(
            String name,
            String category,
            String foodType,
            String description,
            String priceText,
            String quantityText) throws SQLException {

        validateNameAndCategory(name, category);
        validateFoodType(foodType);

        BigDecimal price = parsePrice(priceText);
        int quantity = parseQuantity(quantityText);

        mealDao.create(
            name.trim(),
            category.trim(),
            foodType,
            description,
            price,
            quantity
        );
    }

    public void update(
            int mealId,
            String name,
            String category,
            String foodType,
            String description,
            String priceText,
            String quantityText,
            boolean active) throws SQLException {

        if (mealId <= 0) {
            throw new IllegalArgumentException(
                "Invalid meal selection."
            );
        }

        validateNameAndCategory(name, category);
        validateFoodType(foodType);

        mealDao.update(
            mealId,
            name.trim(),
            category.trim(),
            foodType,
            description,
            parsePrice(priceText),
            parseQuantity(quantityText),
            active
        );
    }

    public void deactivate(int mealId) throws SQLException {
        if (mealId <= 0) {
            throw new IllegalArgumentException(
                "Invalid meal selection."
            );
        }

        mealDao.deactivate(mealId);
    }

    private void validateNameAndCategory(
            String name, String category) {

        if (name == null
                || name.trim().isEmpty()
                || name.trim().length() > 100) {
            throw new IllegalArgumentException(
                "Meal name is required and must not exceed 100 characters."
            );
        }

        if (category == null
                || category.trim().isEmpty()
                || category.trim().length() > 40) {
            throw new IllegalArgumentException(
                "Enter a valid meal category."
            );
        }
    }

    private void validateFoodType(String foodType) {
        if (foodType == null
                || !List.of("VEG", "NON_VEG", "EGG", "UNKNOWN")
                    .contains(foodType)) {
            throw new IllegalArgumentException(
                "Select a valid dietary classification."
            );
        }
    }

    private BigDecimal parsePrice(String text) {
        try {
            BigDecimal price = new BigDecimal(text.trim());

            if (price.signum() < 0 || price.scale() > 2) {
                throw new IllegalArgumentException(
                    "Price must be non-negative with up to "
                    + "two decimal places."
                );
            }

            return price;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException(
                "Enter a valid meal price."
            );
        }
    }

    private int parseQuantity(String text) {
        try {
            int quantity = Integer.parseInt(text.trim());

            if (quantity < 0 || quantity > 100000) {
                throw new IllegalArgumentException(
                    "Stock quantity must be between 0 and 100000."
                );
            }

            return quantity;
        } catch (NumberFormatException | NullPointerException ex) {
            throw new IllegalArgumentException(
                "Enter a valid whole-number stock quantity."
            );
        }
    }
}

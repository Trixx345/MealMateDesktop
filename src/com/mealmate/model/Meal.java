
package com.mealmate.model;

import java.math.BigDecimal;

public record Meal(
    int id,
    String name,
    String category,
    String description,
    BigDecimal price,
    int availableQty,
    boolean active
) {
    public boolean likelyNonVegetarian() {
        String text = (
            (name == null ? "" : name) + " "
            + (description == null ? "" : description)
        ).toLowerCase();

        String[] terms = {
            "chicken", "mutton", "fish", "prawn",
            "shrimp", "beef", "pork", "meat"
        };

        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }

        return false;
    }
}

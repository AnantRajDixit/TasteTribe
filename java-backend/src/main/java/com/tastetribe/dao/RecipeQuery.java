package com.tastetribe.dao;

import com.tastetribe.model.Difficulty;
import com.tastetribe.model.RecipeStatus;

/**
 * Immutable value object carrying the discovery filters for the recipe search —
 * built by the controller from query-string parameters and consumed by
 * {@link RecipeDao#search}. Keeps the DAO signature tidy (one object, not 9 params).
 */
public record RecipeQuery(
        String q,
        String ingredient,
        String cuisine,
        String category,
        Difficulty difficulty,
        String dietary,
        String tag,
        Integer maxTime,
        RecipeStatus status) {
}

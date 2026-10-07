package com.tastetribe.dao;

import java.util.Set;

/**
 * Shared contract for the "user marks recipe" style relations (likes, favorites).
 * A generic interface specialised by {@link LikeDao} and {@link FavoriteDao} —
 * one abstraction, two concrete behaviours (polymorphism).
 */
public interface UserRecipeActionDao {

    /** Apply or remove the relation. Returns the resulting active state. */
    boolean set(String userId, String recipeId, boolean active);

    /** How many users currently have the relation active for a recipe. */
    long countByRecipe(String recipeId);

    /** All recipe ids the user has marked — used to render heart/bookmark state. */
    Set<String> recipeIdsForUser(String userId);

    /** Remove every row for a recipe (cascade on recipe delete). */
    void deleteAllForRecipe(String recipeId);

    /** Remove every row for a user (cascade on account delete). */
    void deleteAllForUser(String userId);

    /** Whether the user currently has the relation active for a recipe. */
    boolean exists(String userId, String recipeId);
}

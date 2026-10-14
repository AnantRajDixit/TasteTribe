package com.tastetribe.dao;

import com.tastetribe.model.Review;
import java.util.Optional;

/** Data-access contract for {@link Review} rows (one per user per recipe). */
public interface ReviewDao extends GenericDao<Review, String> {

    /** All reviews of a recipe, newest first, with author info joined. */
    java.util.List<Review> findByRecipe(String recipeId);

    Optional<Review> findByRecipeAndUser(String recipeId, String userId);

    void update(String recipeId, String userId, int rating, String comment);

    void deleteForRecipe(String recipeId, String userId);

    RatingSummary summariseForRecipe(String recipeId);

    /** Remove all reviews of a recipe (cascade on recipe delete). */
    void deleteAllForRecipe(String recipeId);

    /** Remove all reviews authored by a user (cascade on account delete). */
    void deleteAllForUser(String userId);

    /** Recipe ids the user has reviewed (personalization seeds). */
    java.util.Set<String> recipeIdsForUser(String userId);

    long countAll();

    /** Aggregate row returned by {@link #summariseForRecipe}. */
    record RatingSummary(Double avg, long count) {
    }
}

package com.tastetribe.dao;

import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeStatus;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Data-access contract for {@link Recipe} aggregates (recipe + its ingredient rows). */
public interface RecipeDao extends GenericDao<Recipe, String> {

    /** Full-text-ish filtered search with sorting + pagination. Returns one page. */
    List<Recipe> search(RecipeQuery query, String orderBy, int offset, int limit);

    long countSearch(RecipeQuery query);

    List<Recipe> findByAuthor(String authorId, RecipeStatus status);

    List<Recipe> findByIds(Collection<String> ids);

    List<Recipe> findPublished(int limit);

    long countByStatus(RecipeStatus status);

    long countAll();

    Map<String, Long> countByCategory();

    /** Batched view-count flush used by the asynchronous view tracker. */
    void applyViewDeltas(Map<String, Integer> deltas);

    void updateRating(String recipeId, Double avgRating, int ratingsCount);

    void updateCounter(String recipeId, String column, int value);

    /** Update an existing recipe row and replace its ingredient rows. */
    void update(Recipe recipe);
}

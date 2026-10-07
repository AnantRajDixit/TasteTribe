package com.tastetribe.dao;

import java.util.List;

/** Recently-viewed history per user (dashboard "Cooking History" tab). */
public interface RecentlyViewedDao {

    void upsert(String userId, String recipeId);

    List<String> recentRecipeIds(String userId, int limit);
}

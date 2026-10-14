package com.tastetribe.dao;

import com.tastetribe.model.Comment;
import java.util.List;
import java.util.Optional;

/** Data-access contract for recipe comments. */
public interface CommentDao extends GenericDao<Comment, String> {

    List<Comment> findByRecipe(String recipeId);

    List<Comment> findRecent(int limit);

    void deleteForRecipe(String recipeId);

    void deleteForUser(String userId);

    long countAll();
}

package com.tastetribe.dao.impl;

import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Shared JDBC behaviour for likes/favorites — template-method pattern.
 *
 * <p>Subclasses provide only the table name; all SQL lives here once (inheritance +
 * code reuse). Parameter binding everywhere: no SQL injection surface.</p>
 */
public abstract class AbstractUserRecipeDaoImpl implements com.tastetribe.dao.UserRecipeActionDao {

    private final NamedParameterJdbcTemplate jdbc;

    protected AbstractUserRecipeDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** @return the backing table name ("likes" or "favorites") */
    protected abstract String table();

    @Override
    public boolean set(String userId, String recipeId, boolean active) {
        if (active) {
            jdbc.update("INSERT IGNORE INTO " + table() + " (user_id, recipe_id, created_at) "
                            + "VALUES (:userId, :recipeId, :now)",
                    new MapSqlParameterSource()
                            .addValue("userId", userId)
                            .addValue("recipeId", recipeId)
                            .addValue("now", java.time.LocalDateTime.now()));
            return true;
        }
        jdbc.update("DELETE FROM " + table() + " WHERE user_id = :userId AND recipe_id = :recipeId",
                new MapSqlParameterSource().addValue("userId", userId).addValue("recipeId", recipeId));
        return false;
    }

    @Override
    public long countByRecipe(String recipeId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table() + " WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public Set<String> recipeIdsForUser(String userId) {
        return jdbc.queryForList("SELECT recipe_id FROM " + table() + " WHERE user_id = :userId",
                        new MapSqlParameterSource("userId", userId), String.class)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public void deleteAllForRecipe(String recipeId) {
        jdbc.update("DELETE FROM " + table() + " WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId));
    }

    @Override
    public void deleteAllForUser(String userId) {
        jdbc.update("DELETE FROM " + table() + " WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId));
    }

    @Override
    public boolean exists(String userId, String recipeId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + table() + " WHERE user_id = :userId AND recipe_id = :recipeId",
                new MapSqlParameterSource().addValue("userId", userId).addValue("recipeId", recipeId), Long.class);
        return count != null && count > 0;
    }
}

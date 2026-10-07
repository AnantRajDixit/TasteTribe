package com.tastetribe.dao.impl;

import com.tastetribe.dao.RecentlyViewedDao;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link RecentlyViewedDao}. */
@Repository
public class RecentlyViewedDaoImpl implements RecentlyViewedDao {

    private final NamedParameterJdbcTemplate jdbc;

    public RecentlyViewedDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void upsert(String userId, String recipeId) {
        jdbc.update("INSERT INTO recently_viewed (user_id, recipe_id, viewed_at) VALUES (:u, :r, :v) "
                        + "ON DUPLICATE KEY UPDATE viewed_at = :v",
                new MapSqlParameterSource()
                        .addValue("u", userId)
                        .addValue("r", recipeId)
                        .addValue("v", java.time.LocalDateTime.now()));
    }

    @Override
    public List<String> recentRecipeIds(String userId, int limit) {
        return jdbc.queryForList(
                "SELECT recipe_id FROM recently_viewed WHERE user_id = :u ORDER BY viewed_at DESC LIMIT :lim",
                new MapSqlParameterSource().addValue("u", userId).addValue("lim", limit), String.class);
    }
}

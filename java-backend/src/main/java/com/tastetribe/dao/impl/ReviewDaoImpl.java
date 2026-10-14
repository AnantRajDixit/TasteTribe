package com.tastetribe.dao.impl;

import com.tastetribe.dao.ReviewDao;
import com.tastetribe.model.Review;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link ReviewDao} (author info joined from users). */
@Repository
public class ReviewDaoImpl implements ReviewDao {

    private static final String SELECT = """
            SELECT r.id, r.recipe_id, r.user_id, r.rating, r.comment, r.created_at, r.updated_at,
                   u.username, u.name AS user_name, u.avatar_url
            FROM reviews r LEFT JOIN users u ON u.id = r.user_id
            """;

    private static final RowMapper<Review> MAPPER = (rs, rowNum) -> mapRow(rs);

    private final NamedParameterJdbcTemplate jdbc;

    public ReviewDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getString("id"));
        review.setRecipeId(rs.getString("recipe_id"));
        review.setUserId(rs.getString("user_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));
        review.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        review.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        review.setUsername(rs.getString("username"));
        review.setName(rs.getString("user_name"));
        review.setAvatarUrl(rs.getString("avatar_url"));
        return review;
    }

    @Override
    public List<Review> findByRecipe(String recipeId) {
        return jdbc.query(SELECT + " WHERE r.recipe_id = :recipeId ORDER BY r.created_at DESC",
                new MapSqlParameterSource("recipeId", recipeId), MAPPER);
    }

    @Override
    public Optional<Review> findByRecipeAndUser(String recipeId, String userId) {
        List<Review> rows = jdbc.query(SELECT + " WHERE r.recipe_id = :recipeId AND r.user_id = :userId",
                new MapSqlParameterSource().addValue("recipeId", recipeId).addValue("userId", userId), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public void save(Review review) {
        jdbc.update(
                "INSERT INTO reviews (id, recipe_id, user_id, rating, comment, created_at, updated_at) "
                        + "VALUES (:id, :recipeId, :userId, :rating, :comment, :createdAt, :updatedAt)",
                new MapSqlParameterSource()
                        .addValue("id", review.getId())
                        .addValue("recipeId", review.getRecipeId())
                        .addValue("userId", review.getUserId())
                        .addValue("rating", review.getRating())
                        .addValue("comment", review.getComment())
                        .addValue("createdAt", review.getCreatedAt())
                        .addValue("updatedAt", review.getUpdatedAt()));
    }

    @Override
    public void update(String recipeId, String userId, int rating, String comment) {
        jdbc.update(
                "UPDATE reviews SET rating = :rating, comment = :comment, updated_at = :now "
                        + "WHERE recipe_id = :recipeId AND user_id = :userId",
                new MapSqlParameterSource()
                        .addValue("rating", rating)
                        .addValue("comment", comment)
                        .addValue("now", java.time.LocalDateTime.now())
                        .addValue("recipeId", recipeId)
                        .addValue("userId", userId));
    }

    @Override
    public void deleteForRecipe(String recipeId, String userId) {
        jdbc.update("DELETE FROM reviews WHERE recipe_id = :recipeId AND user_id = :userId",
                new MapSqlParameterSource().addValue("recipeId", recipeId).addValue("userId", userId));
    }

    @Override
    public RatingSummary summariseForRecipe(String recipeId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT AVG(rating) AS avg_rating, COUNT(*) AS n FROM reviews WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId));
        if (rows.isEmpty() || rows.get(0).get("n") == null || ((Number) rows.get(0).get("n")).longValue() == 0) {
            return new RatingSummary(null, 0);
        }
        Double avg = rows.get(0).get("avg_rating") == null ? null
                : ((Number) rows.get(0).get("avg_rating")).doubleValue();
        long n = ((Number) rows.get(0).get("n")).longValue();
        return new RatingSummary(avg, n);
    }

    @Override
    public List<Review> findAll() {
        return jdbc.query(SELECT + " ORDER BY r.created_at DESC LIMIT 500", new MapSqlParameterSource(), MAPPER);
    }

    @Override
    public Optional<Review> findById(String id) {
        List<Review> rows = jdbc.query(SELECT + " WHERE r.id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM reviews WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public void deleteAllForRecipe(String recipeId) {
        jdbc.update("DELETE FROM reviews WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId));
    }

    @Override
    public void deleteAllForUser(String userId) {
        jdbc.update("DELETE FROM reviews WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId));
    }

    @Override
    public java.util.Set<String> recipeIdsForUser(String userId) {
        return new java.util.HashSet<>(jdbc.queryForList(
                "SELECT recipe_id FROM reviews WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId), String.class));
    }

    @Override
    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM reviews", new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }
}

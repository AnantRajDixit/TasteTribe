package com.tastetribe.dao.impl;

import com.tastetribe.dao.CommentDao;
import com.tastetribe.model.Comment;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link CommentDao} (author info joined from users). */
@Repository
public class CommentDaoImpl implements CommentDao {

    private static final String SELECT = """
            SELECT c.id, c.recipe_id, c.user_id, c.text, c.created_at,
                   u.username, u.name AS user_name, u.avatar_url
            FROM comments c LEFT JOIN users u ON u.id = c.user_id
            """;

    private static final RowMapper<Comment> MAPPER = (rs, rowNum) -> mapRow(rs);

    private final NamedParameterJdbcTemplate jdbc;

    public CommentDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static Comment mapRow(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getString("id"));
        comment.setRecipeId(rs.getString("recipe_id"));
        comment.setUserId(rs.getString("user_id"));
        comment.setText(rs.getString("text"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        comment.setUsername(rs.getString("username"));
        comment.setName(rs.getString("user_name"));
        comment.setAvatarUrl(rs.getString("avatar_url"));
        return comment;
    }

    @Override
    public List<Comment> findByRecipe(String recipeId) {
        return jdbc.query(SELECT + " WHERE c.recipe_id = :recipeId ORDER BY c.created_at ASC",
                new MapSqlParameterSource("recipeId", recipeId), MAPPER);
    }

    @Override
    public List<Comment> findRecent(int limit) {
        return jdbc.query(SELECT + " ORDER BY c.created_at DESC LIMIT :limit",
                new MapSqlParameterSource("limit", limit), MAPPER);
    }

    @Override
    public Optional<Comment> findById(String id) {
        List<Comment> rows = jdbc.query(SELECT + " WHERE c.id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public void save(Comment comment) {
        jdbc.update(
                "INSERT INTO comments (id, recipe_id, user_id, text, created_at) "
                        + "VALUES (:id, :recipeId, :userId, :text, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", comment.getId())
                        .addValue("recipeId", comment.getRecipeId())
                        .addValue("userId", comment.getUserId())
                        .addValue("text", comment.getText())
                        .addValue("createdAt", comment.getCreatedAt()));
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM comments WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public void deleteForRecipe(String recipeId) {
        jdbc.update("DELETE FROM comments WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId));
    }

    @Override
    public void deleteForUser(String userId) {
        jdbc.update("DELETE FROM comments WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId));
    }

    @Override
    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM comments", new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public List<Comment> findAll() {
        return findRecent(500);
    }
}

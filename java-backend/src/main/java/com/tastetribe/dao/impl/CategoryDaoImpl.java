package com.tastetribe.dao.impl;

import com.tastetribe.dao.CategoryDao;
import com.tastetribe.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link CategoryDao}. */
@Repository
public class CategoryDaoImpl implements CategoryDao {

    private static final RowMapper<Category> MAPPER = (rs, rowNum) -> {
        Category category = new Category(rs.getString("id"), rs.getTimestamp("created_at").toLocalDateTime());
        category.setName(rs.getString("name"));
        category.setSlug(rs.getString("slug"));
        category.setDescription(rs.getString("description"));
        category.setImageUrl(rs.getString("image_url"));
        return category;
    };

    private static final String COLS = "id, name, slug, description, image_url, created_at";

    private final NamedParameterJdbcTemplate jdbc;

    public CategoryDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Category> findById(String id) {
        List<Category> rows = jdbc.query("SELECT " + COLS + " FROM categories WHERE id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        List<Category> rows = jdbc.query("SELECT " + COLS + " FROM categories WHERE slug = :slug",
                new MapSqlParameterSource("slug", slug), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public boolean existsBySlug(String slug) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM categories WHERE slug = :slug",
                new MapSqlParameterSource("slug", slug), Long.class);
        return count != null && count > 0;
    }

    @Override
    public List<Category> findAll() {
        return jdbc.query("SELECT " + COLS + " FROM categories ORDER BY name",
                new MapSqlParameterSource(), MAPPER);
    }

    @Override
    public void save(Category category) {
        jdbc.update(
                "INSERT INTO categories (id, name, slug, description, image_url, created_at) "
                        + "VALUES (:id, :name, :slug, :description, :imageUrl, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", category.getId())
                        .addValue("name", category.getName())
                        .addValue("slug", category.getSlug())
                        .addValue("description", category.getDescription())
                        .addValue("imageUrl", category.getImageUrl())
                        .addValue("createdAt", category.getCreatedAt()));
    }

    @Override
    public void update(Category category) {
        jdbc.update(
                "UPDATE categories SET name = :name, slug = :slug, description = :description, image_url = :imageUrl "
                        + "WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", category.getId())
                        .addValue("name", category.getName())
                        .addValue("slug", category.getSlug())
                        .addValue("description", category.getDescription())
                        .addValue("imageUrl", category.getImageUrl()));
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM categories WHERE id = :id", new MapSqlParameterSource("id", id));
    }
}

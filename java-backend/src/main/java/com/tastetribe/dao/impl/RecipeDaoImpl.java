package com.tastetribe.dao.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.RecipeQuery;
import com.tastetribe.model.Difficulty;
import com.tastetribe.model.Nutrition;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.RecipeStatus;
import com.tastetribe.util.JsonSupport;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC implementation of {@link RecipeDao}.
 *
 * <p>The dynamic WHERE clause for discovery is assembled from whitelisted columns and
 * every value is bound as a parameter — SQL injection is impossible even though the
 * query shape varies. Ingredients live in the normalised child table and are attached
 * with one batched IN query (no N+1).</p>
 */
@Repository
public class RecipeDaoImpl implements RecipeDao {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private static final String COLS = "id, title, description, cover_image, cuisine, category, difficulty, dietary, "
            + "prep_time, cook_time, total_time, servings, status, source, views, likes_count, favorites_count, "
            + "avg_rating, ratings_count, tags_json, instructions_json, nutrition_json, author_id, author_name, "
            + "author_username, created_at, updated_at";

    private static final RowMapper<Recipe> MAPPER = new RowMapper<>() {
        @Override
        public Recipe mapRow(ResultSet rs, int rowNum) throws SQLException {
            Recipe recipe = new Recipe();
            recipe.setId(rs.getString("id"));
            recipe.setTitle(rs.getString("title"));
            recipe.setDescription(rs.getString("description"));
            recipe.setCoverImage(rs.getString("cover_image"));
            recipe.setCuisine(rs.getString("cuisine"));
            recipe.setCategory(rs.getString("category"));
            recipe.setDifficulty(Difficulty.from(rs.getString("difficulty")));
            recipe.setDietary(rs.getString("dietary"));
            recipe.setPrepTime(rs.getInt("prep_time"));
            recipe.setCookTime(rs.getInt("cook_time"));
            recipe.setTotalTime(rs.getInt("total_time"));
            recipe.setServings(rs.getInt("servings"));
            recipe.setStatus(RecipeStatus.from(rs.getString("status")));
            recipe.setSource(rs.getString("source"));
            recipe.setViews(rs.getInt("views"));
            recipe.setLikesCount(rs.getInt("likes_count"));
            recipe.setFavoritesCount(rs.getInt("favorites_count"));
            double rating = rs.getDouble("avg_rating");
            recipe.setAvgRating(rs.wasNull() ? null : rating);
            recipe.setRatingsCount(rs.getInt("ratings_count"));
            recipe.setAuthorId(rs.getString("author_id"));
            recipe.setAuthorName(rs.getString("author_name"));
            recipe.setAuthorUsername(rs.getString("author_username"));
            recipe.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            recipe.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            return recipe;
        }
    };

    private final NamedParameterJdbcTemplate jdbc;
    private final JsonSupport json;

    public RecipeDaoImpl(NamedParameterJdbcTemplate jdbc, JsonSupport json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    // ---------- JSON column bridging ----------

    private void fillJsonFields(Recipe recipe, ResultSet rs) throws SQLException {
        recipe.setTags(json.toStringList(rs.getString("tags_json")));
        recipe.setInstructions(json.toStringList(rs.getString("instructions_json")));
        recipe.setNutrition(json.toNutrition(rs.getString("nutrition_json")));
    }

    private List<Recipe> mapMany(String sql, MapSqlParameterSource params) {
        List<Recipe> recipes = jdbc.query(sql, params, rs -> {
            List<Recipe> out = new ArrayList<>();
            while (rs.next()) {
                Recipe recipe = MAPPER.mapRow(rs, out.size());
                fillJsonFields(recipe, rs);
                out.add(recipe);
            }
            return out;
        });
        attachIngredients(recipes);
        return recipes;
    }

    private void attachIngredients(List<Recipe> recipes) {
        if (recipes.isEmpty()) {
            return;
        }
        List<String> ids = recipes.stream().map(Recipe::getId).toList();
        MapSqlParameterSource params = new MapSqlParameterSource("ids", ids);
        List<RecipeIngredient> rows = jdbc.query(
                "SELECT id, recipe_id, name, quantity, unit, is_optional, position "
                        + "FROM recipe_ingredients WHERE recipe_id IN (:ids) ORDER BY recipe_id, position",
                params,
                (rs, i) -> {
                    RecipeIngredient ing = new RecipeIngredient();
                    ing.setId(rs.getLong("id"));
                    ing.setRecipeId(rs.getString("recipe_id"));
                    ing.setName(rs.getString("name"));
                    ing.setQuantity(rs.getDouble("quantity"));
                    ing.setUnit(rs.getString("unit"));
                    ing.setOptional(rs.getBoolean("is_optional"));
                    ing.setPosition(rs.getInt("position"));
                    return ing;
                });
        Map<String, Recipe> byId = new HashMap<>();
        recipes.forEach(recipe -> byId.put(recipe.getId(), recipe));
        rows.forEach(ing -> {
            Recipe owner = byId.get(ing.getRecipeId());
            if (owner != null) {
                owner.getIngredients().add(ing);
            }
        });
    }

    // ---------- GenericDao ----------

    @Override
    public List<Recipe> findAll() {
        return mapMany("SELECT " + COLS + " FROM recipes ORDER BY created_at DESC LIMIT 500",
                new MapSqlParameterSource());
    }

    @Override
    @Transactional
    public void save(Recipe recipe) {
        jdbc.update(INSERT_SQL, bind(recipe));
        insertIngredients(recipe);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jdbc.update("DELETE FROM recipe_ingredients WHERE recipe_id = :id", new MapSqlParameterSource("id", id));
        jdbc.update("DELETE FROM recipes WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    // ---------- Search ----------

    @Override
    public List<Recipe> search(RecipeQuery query, String orderBy, int offset, int limit) {
        SearchClause clause = buildWhere(query);
        String sql = "SELECT " + COLS + " FROM recipes WHERE " + clause.where()
                + " ORDER BY " + orderBy + " LIMIT :limit OFFSET :offset";
        MapSqlParameterSource params = clause.params()
                .addValue("limit", limit)
                .addValue("offset", offset);
        return mapMany(sql, params);
    }

    @Override
    public long countSearch(RecipeQuery query) {
        SearchClause clause = buildWhere(query);
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM recipes WHERE " + clause.where(),
                clause.params(), Long.class);
        return count == null ? 0 : count;
    }

    /** Whitelisted sort expressions — the sort key never touches SQL as raw text. */
    public static String orderByFor(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "oldest" -> "created_at ASC";
            case "rating" -> "avg_rating DESC, ratings_count DESC";
            case "popular" -> "likes_count DESC, views DESC";
            case "time" -> "total_time ASC";
            default -> "created_at DESC";
        };
    }

    private record SearchClause(String where, MapSqlParameterSource params) {
    }

    private SearchClause buildWhere(RecipeQuery query) {
        List<String> conditions = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();
        conditions.add("status = :status");
        params.addValue("status",
                (query.status() == null ? RecipeStatus.PUBLISHED : query.status()).name());
        if (query.q() != null && !query.q().isBlank()) {
            conditions.add("(title LIKE :q OR description LIKE :q OR tags_json LIKE :q)");
            params.addValue("q", "%" + query.q().trim() + "%");
        }
        if (query.ingredient() != null && !query.ingredient().isBlank()) {
            conditions.add("id IN (SELECT recipe_id FROM recipe_ingredients WHERE name LIKE :ing)");
            params.addValue("ing", "%" + query.ingredient().trim() + "%");
        }
        if (query.cuisine() != null && !query.cuisine().isBlank()) {
            conditions.add("cuisine = :cuisine");
            params.addValue("cuisine", query.cuisine());
        }
        if (query.category() != null && !query.category().isBlank()) {
            conditions.add("category = :category");
            params.addValue("category", query.category());
        }
        if (query.difficulty() != null) {
            conditions.add("difficulty = :difficulty");
            params.addValue("difficulty", query.difficulty().getLabel());
        }
        if (query.dietary() != null && !query.dietary().isBlank()) {
            conditions.add("dietary = :dietary");
            params.addValue("dietary", query.dietary());
        }
        if (query.tag() != null && !query.tag().isBlank()) {
            conditions.add("tags_json LIKE :tag");
            params.addValue("tag", "%" + query.tag().trim() + "%");
        }
        if (query.maxTime() != null && query.maxTime() > 0) {
            conditions.add("total_time <= :maxTime");
            params.addValue("maxTime", query.maxTime());
        }
        return new SearchClause(String.join(" AND ", conditions), params);
    }

    // ---------- Author / counters ----------

    @Override
    public List<Recipe> findByAuthor(String authorId, RecipeStatus status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("authorId", authorId);
        String sql = "SELECT " + COLS + " FROM recipes WHERE author_id = :authorId";
        if (status != null) {
            sql += " AND status = :status";
            params.addValue("status", status.name());
        }
        sql += " ORDER BY created_at DESC";
        return mapMany(sql, params);
    }

    @Override
    public List<Recipe> findByIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return mapMany("SELECT " + COLS + " FROM recipes WHERE id IN (:ids)",
                new MapSqlParameterSource("ids", ids));
    }

    @Override
    public List<Recipe> findPublished(int limit) {
        return mapMany("SELECT " + COLS + " FROM recipes WHERE status = 'PUBLISHED' "
                        + "ORDER BY created_at DESC LIMIT :limit",
                new MapSqlParameterSource("limit", limit));
    }

    @Override
    public long countByStatus(RecipeStatus status) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM recipes WHERE status = :status",
                new MapSqlParameterSource("status", status.name()), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM recipes", new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public Map<String, Long> countByCategory() {
        Map<String, Long> out = new LinkedHashMap<>();
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT category, COUNT(*) AS n FROM recipes WHERE status = 'PUBLISHED' GROUP BY category",
                new MapSqlParameterSource());
        for (Map<String, Object> row : rows) {
            out.put((String) row.get("category"), ((Number) row.get("n")).longValue());
        }
        return out;
    }

    @Override
    public void applyViewDeltas(Map<String, Integer> deltas) {
        deltas.forEach((id, delta) -> jdbc.update(
                "UPDATE recipes SET views = views + :delta WHERE id = :id",
                new MapSqlParameterSource().addValue("id", id).addValue("delta", delta)));
    }

    @Override
    public void updateRating(String recipeId, Double avgRating, int ratingsCount) {
        jdbc.update(
                "UPDATE recipes SET avg_rating = :avg, ratings_count = :count WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", recipeId)
                        .addValue("avg", avgRating)
                        .addValue("count", ratingsCount));
    }

    @Override
    public void updateCounter(String recipeId, String column, int value) {
        // Column comes from a service-layer whitelist, value is bound — safe.
        jdbc.update("UPDATE recipes SET " + column + " = :value WHERE id = :id",
                new MapSqlParameterSource().addValue("id", recipeId).addValue("value", value));
    }

    @Override
    @Transactional
    public void update(Recipe recipe) {
        jdbc.update(UPDATE_SQL, bind(recipe));
        jdbc.update("DELETE FROM recipe_ingredients WHERE recipe_id = :id",
                new MapSqlParameterSource("id", recipe.getId()));
        insertIngredients(recipe);
    }

    // ---------- SQL fragments ----------

    private static final String INSERT_SQL = """
            INSERT INTO recipes (id, title, description, cover_image, cuisine, category, difficulty, dietary,
                prep_time, cook_time, total_time, servings, status, source, views, likes_count, favorites_count,
                avg_rating, ratings_count, tags_json, instructions_json, nutrition_json, author_id, author_name,
                author_username, created_at, updated_at)
            VALUES (:id, :title, :description, :coverImage, :cuisine, :category, :difficulty, :dietary,
                :prepTime, :cookTime, :totalTime, :servings, :status, :source, :views, :likesCount, :favoritesCount,
                :avgRating, :ratingsCount, :tagsJson, :instructionsJson, :nutritionJson, :authorId, :authorName,
                :authorUsername, :createdAt, :updatedAt)
            """;

    private static final String UPDATE_SQL = """
            UPDATE recipes SET title = :title, description = :description, cover_image = :coverImage,
                cuisine = :cuisine, category = :category, difficulty = :difficulty, dietary = :dietary,
                prep_time = :prepTime, cook_time = :cookTime, total_time = :totalTime, servings = :servings,
                status = :status, tags_json = :tagsJson, instructions_json = :instructionsJson,
                nutrition_json = :nutritionJson, updated_at = :updatedAt
            WHERE id = :id
            """;

    private MapSqlParameterSource bind(Recipe recipe) {
        return new MapSqlParameterSource()
                .addValue("id", recipe.getId())
                .addValue("title", recipe.getTitle())
                .addValue("description", recipe.getDescription())
                .addValue("coverImage", recipe.getCoverImage())
                .addValue("cuisine", recipe.getCuisine())
                .addValue("category", recipe.getCategory())
                .addValue("difficulty", recipe.getDifficulty().getLabel())
                .addValue("dietary", recipe.getDietary())
                .addValue("prepTime", recipe.getPrepTime())
                .addValue("cookTime", recipe.getCookTime())
                .addValue("totalTime", recipe.getTotalTime())
                .addValue("servings", recipe.getServings())
                .addValue("status", recipe.getStatus().name())
                .addValue("source", recipe.getSource())
                .addValue("views", recipe.getViews())
                .addValue("likesCount", recipe.getLikesCount())
                .addValue("favoritesCount", recipe.getFavoritesCount())
                .addValue("avgRating", recipe.getAvgRating())
                .addValue("ratingsCount", recipe.getRatingsCount())
                .addValue("tagsJson", json.toJson(recipe.getTags()))
                .addValue("instructionsJson", json.toJson(recipe.getInstructions()))
                .addValue("nutritionJson", json.toJson(recipe.getNutrition()))
                .addValue("authorId", recipe.getAuthorId())
                .addValue("authorName", recipe.getAuthorName())
                .addValue("authorUsername", recipe.getAuthorUsername())
                .addValue("createdAt", recipe.getCreatedAt())
                .addValue("updatedAt", recipe.getUpdatedAt());
    }

    private void insertIngredients(Recipe recipe) {
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        if (ingredients == null || ingredients.isEmpty()) {
            return;
        }
        int position = 0;
        for (RecipeIngredient ing : ingredients) {
            jdbc.update(
                    "INSERT INTO recipe_ingredients (recipe_id, name, quantity, unit, is_optional, position) "
                            + "VALUES (:recipeId, :name, :quantity, :unit, :optional, :position)",
                    new MapSqlParameterSource()
                            .addValue("recipeId", recipe.getId())
                            .addValue("name", ing.getName())
                            .addValue("quantity", ing.getQuantity())
                            .addValue("unit", ing.getUnit())
                            .addValue("optional", ing.isOptional())
                            .addValue("position", position++));
        }
    }

    @Override
    public Optional<Recipe> findById(String id) {
        List<Recipe> rows = mapMany("SELECT " + COLS + " FROM recipes WHERE id = :id",
                new MapSqlParameterSource("id", id));
        return rows.stream().findFirst();
    }
}

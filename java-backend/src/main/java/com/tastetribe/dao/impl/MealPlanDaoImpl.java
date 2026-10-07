package com.tastetribe.dao.impl;

import com.tastetribe.dao.MealPlanDao;
import com.tastetribe.model.MealPlanEntry;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link MealPlanDao}. The recipe title/image/time are joined
 * from {@code recipes} so the planner grid needs exactly one query per week.
 */
@Repository
public class MealPlanDaoImpl implements MealPlanDao {

    private static final String SELECT = """
            SELECT m.id, m.user_id, m.recipe_id, m.plan_date, m.slot, m.servings, m.created_at,
                   r.title AS recipe_title, r.cover_image, r.total_time
            FROM meal_plan_entries m LEFT JOIN recipes r ON r.id = m.recipe_id
            """;

    private static final RowMapper<MealPlanEntry> MAPPER = (rs, rowNum) -> mapRow(rs);

    private final NamedParameterJdbcTemplate jdbc;

    public MealPlanDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static MealPlanEntry mapRow(ResultSet rs) throws SQLException {
        MealPlanEntry entry = new MealPlanEntry(rs.getString("id"),
                rs.getTimestamp("created_at").toLocalDateTime());
        entry.setUserId(rs.getString("user_id"));
        entry.setRecipeId(rs.getString("recipe_id"));
        entry.setPlanDate(rs.getDate("plan_date").toLocalDate());
        entry.setSlot(rs.getString("slot"));
        entry.setServings(rs.getInt("servings"));
        entry.setRecipeTitle(rs.getString("recipe_title"));
        entry.setRecipeImage(rs.getString("cover_image"));
        entry.setRecipeTotalTime(rs.getInt("total_time"));
        return entry;
    }

    @Override
    public List<MealPlanEntry> findInRange(String userId, LocalDate from, LocalDate to) {
        return jdbc.query(
                SELECT + " WHERE m.user_id = :userId AND m.plan_date BETWEEN :from AND :to "
                        + "ORDER BY m.plan_date, m.slot",
                new MapSqlParameterSource()
                        .addValue("userId", userId)
                        .addValue("from", from)
                        .addValue("to", to),
                MAPPER);
    }

    @Override
    public Optional<MealPlanEntry> findById(String id) {
        List<MealPlanEntry> rows = jdbc.query(SELECT + " WHERE m.id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public void save(MealPlanEntry entry) {
        jdbc.update(
                "INSERT INTO meal_plan_entries (id, user_id, recipe_id, plan_date, slot, servings, created_at) "
                        + "VALUES (:id, :userId, :recipeId, :planDate, :slot, :servings, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", entry.getId())
                        .addValue("userId", entry.getUserId())
                        .addValue("recipeId", entry.getRecipeId())
                        .addValue("planDate", entry.getPlanDate())
                        .addValue("slot", entry.getSlot())
                        .addValue("servings", entry.getServings())
                        .addValue("createdAt", entry.getCreatedAt()));
    }

    @Override
    public void move(String id, LocalDate planDate, String slot) {
        jdbc.update("UPDATE meal_plan_entries SET plan_date = :planDate, slot = :slot WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("planDate", planDate)
                        .addValue("slot", slot));
    }

    @Override
    public void deleteById(String id) {
        jdbc.update("DELETE FROM meal_plan_entries WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public void deleteAllForUser(String userId) {
        jdbc.update("DELETE FROM meal_plan_entries WHERE user_id = :userId",
                new MapSqlParameterSource("userId", userId));
    }
}

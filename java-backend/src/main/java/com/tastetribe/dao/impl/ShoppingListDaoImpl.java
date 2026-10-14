package com.tastetribe.dao.impl;

import com.tastetribe.dao.ShoppingListDao;
import com.tastetribe.model.ShoppingItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** JDBC implementation of {@link ShoppingListDao} (replace-all persistence). */
@Repository
public class ShoppingListDaoImpl implements ShoppingListDao {

    private static final RowMapper<ShoppingItem> MAPPER = ShoppingListDaoImpl::mapRow;

    private final NamedParameterJdbcTemplate jdbc;

    public ShoppingListDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static ShoppingItem mapRow(ResultSet rs, int i) throws SQLException {
        return new ShoppingItem(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getString("name"),
                rs.getDouble("quantity"),
                rs.getString("unit"),
                rs.getBoolean("checked"),
                rs.getString("recipe_title"),
                rs.getInt("position"));
    }

    @Override
    public List<ShoppingItem> findByUser(String userId) {
        return jdbc.query("SELECT id, user_id, name, quantity, unit, checked, recipe_title, position "
                        + "FROM shopping_list_items WHERE user_id = :u ORDER BY position",
                new MapSqlParameterSource("u", userId), MAPPER);
    }

    @Override
    @Transactional
    public void replaceAll(String userId, List<ShoppingItem> items) {
        jdbc.update("DELETE FROM shopping_list_items WHERE user_id = :u",
                new MapSqlParameterSource("u", userId));
        int position = 0;
        for (ShoppingItem item : items) {
            jdbc.update(
                    "INSERT INTO shopping_list_items (id, user_id, name, quantity, unit, checked, recipe_title, position) "
                            + "VALUES (:id, :u, :name, :q, :unit, :checked, :recipeTitle, :pos)",
                    new MapSqlParameterSource()
                            .addValue("id", item.getId())
                            .addValue("u", userId)
                            .addValue("name", item.getName())
                            .addValue("q", item.getQuantity())
                            .addValue("unit", item.getUnit())
                            .addValue("checked", item.isChecked())
                            .addValue("recipeTitle", item.getRecipeTitle())
                            .addValue("pos", position++));
        }
    }
}

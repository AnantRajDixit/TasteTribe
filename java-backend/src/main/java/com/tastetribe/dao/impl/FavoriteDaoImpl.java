package com.tastetribe.dao.impl;

import com.tastetribe.dao.FavoriteDao;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation backed by the {@code favorites} table. */
@Repository
public class FavoriteDaoImpl extends AbstractUserRecipeDaoImpl implements FavoriteDao {

    public FavoriteDaoImpl(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected String table() {
        return "favorites";
    }
}

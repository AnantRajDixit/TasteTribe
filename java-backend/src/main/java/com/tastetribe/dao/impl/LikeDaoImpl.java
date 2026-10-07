package com.tastetribe.dao.impl;

import com.tastetribe.dao.LikeDao;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation backed by the {@code likes} table. */
@Repository
public class LikeDaoImpl extends AbstractUserRecipeDaoImpl implements LikeDao {

    public LikeDaoImpl(NamedParameterJdbcTemplate jdbc) {
        super(jdbc);
    }

    @Override
    protected String table() {
        return "likes";
    }
}

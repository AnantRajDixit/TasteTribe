package com.tastetribe.dao.impl;

import com.tastetribe.dao.SessionDao;
import com.tastetribe.model.PasswordReset;
import com.tastetribe.model.SessionToken;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link SessionDao}. */
@Repository
public class SessionDaoImpl implements SessionDao {

    private final NamedParameterJdbcTemplate jdbc;

    public SessionDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void saveSession(SessionToken session) {
        jdbc.update(
                "INSERT INTO sessions (token, user_id, created_at, expires_at) "
                        + "VALUES (:token, :userId, :createdAt, :expiresAt)",
                new MapSqlParameterSource()
                        .addValue("token", session.getToken())
                        .addValue("userId", session.getUserId())
                        .addValue("createdAt", session.getCreatedAt())
                        .addValue("expiresAt", session.getExpiresAt()));
    }

    @Override
    public Optional<SessionToken> findSession(String token) {
        List<SessionToken> rows = jdbc.query(
                "SELECT token, user_id, created_at, expires_at FROM sessions WHERE token = :token",
                new MapSqlParameterSource("token", token),
                (rs, i) -> new SessionToken(rs.getString("token"), rs.getString("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at").toLocalDateTime()));
        return rows.stream().findFirst();
    }

    @Override
    public void deleteSession(String token) {
        jdbc.update("DELETE FROM sessions WHERE token = :token", new MapSqlParameterSource("token", token));
    }

    @Override
    public void deleteSessionsForUser(String userId) {
        jdbc.update("DELETE FROM sessions WHERE user_id = :userId", new MapSqlParameterSource("userId", userId));
    }

    @Override
    public void deleteSessionsForUserExcept(String userId, String keepToken) {
        jdbc.update("DELETE FROM sessions WHERE user_id = :userId AND token <> :keepToken",
                new MapSqlParameterSource().addValue("userId", userId).addValue("keepToken", keepToken));
    }

    @Override
    public void saveResetToken(PasswordReset reset) {
        jdbc.update(
                "INSERT INTO password_resets (token, user_id, created_at, expires_at) "
                        + "VALUES (:token, :userId, :createdAt, :expiresAt)",
                new MapSqlParameterSource()
                        .addValue("token", reset.getToken())
                        .addValue("userId", reset.getUserId())
                        .addValue("createdAt", reset.getCreatedAt())
                        .addValue("expiresAt", reset.getExpiresAt()));
    }

    @Override
    public Optional<PasswordReset> findResetToken(String token) {
        List<PasswordReset> rows = jdbc.query(
                "SELECT token, user_id, created_at, expires_at FROM password_resets WHERE token = :token",
                new MapSqlParameterSource("token", token),
                (rs, i) -> new PasswordReset(rs.getString("token"), rs.getString("user_id"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("expires_at").toLocalDateTime()));
        return rows.stream().findFirst();
    }

    @Override
    public void deleteResetToken(String token) {
        jdbc.update("DELETE FROM password_resets WHERE token = :token", new MapSqlParameterSource("token", token));
    }
}

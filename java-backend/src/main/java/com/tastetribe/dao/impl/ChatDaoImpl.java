package com.tastetribe.dao.impl;

import com.tastetribe.dao.ChatDao;
import com.tastetribe.model.ChatMessage;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link ChatDao} — AI conversation history persistence. */
@Repository
public class ChatDaoImpl implements ChatDao {

    private final NamedParameterJdbcTemplate jdbc;

    public ChatDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(ChatMessage message) {
        jdbc.update(
                "INSERT INTO ai_messages (id, user_id, session_id, role, content, created_at) "
                        + "VALUES (:id, :userId, :sessionId, :role, :content, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", message.getId())
                        .addValue("userId", message.getUserId())
                        .addValue("sessionId", message.getSessionId())
                        .addValue("role", message.getRole())
                        .addValue("content", message.getContent())
                        .addValue("createdAt", message.getCreatedAt()));
    }

    @Override
    public List<ChatMessage> findRecent(String sessionId, String userId, int limit) {
        // Sub-select keeps the newest N messages, ordered oldest-first for replay.
        return jdbc.query(
                "SELECT id, user_id, session_id, role, content, created_at FROM ("
                        + "  SELECT * FROM ai_messages WHERE session_id = :sid AND user_id = :uid "
                        + "  ORDER BY created_at DESC LIMIT :lim"
                        + ") recent ORDER BY created_at ASC",
                new MapSqlParameterSource()
                        .addValue("sid", sessionId)
                        .addValue("uid", userId)
                        .addValue("lim", limit),
                (rs, i) -> new ChatMessage(rs.getString("id"), rs.getString("user_id"),
                        rs.getString("session_id"), rs.getString("role"), rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()));
    }
}

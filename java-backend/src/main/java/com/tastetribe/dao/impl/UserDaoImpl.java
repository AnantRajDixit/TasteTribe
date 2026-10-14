package com.tastetribe.dao.impl;

import com.tastetribe.dao.UserDao;
import com.tastetribe.model.Role;
import com.tastetribe.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * JDBC implementation of {@link UserDao}.
 *
 * <p>All access goes through {@link NamedParameterJdbcTemplate}: values are ALWAYS
 * bound as parameters, never concatenated into SQL — the standard protection against
 * SQL injection.</p>
 */
@Repository
public class UserDaoImpl implements UserDao {

    private static final RowMapper<User> MAPPER = UserDaoImpl::mapRow;

    private final NamedParameterJdbcTemplate jdbc;

    public UserDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static User mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setAvatarUrl(rs.getString("avatar_url"));
        user.setBio(rs.getString("bio"));
        user.setRole(Role.from(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return user;
    }

    private static final String COLS = "id, name, username, email, password_hash, avatar_url, bio, role, created_at";

    @Override
    public Optional<User> findById(String id) {
        List<User> rows = jdbc.query(
                "SELECT " + COLS + " FROM users WHERE id = :id",
                new MapSqlParameterSource("id", id), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        List<User> rows = jdbc.query(
                "SELECT " + COLS + " FROM users WHERE username = :username",
                new MapSqlParameterSource("username", username.toLowerCase()), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> rows = jdbc.query(
                "SELECT " + COLS + " FROM users WHERE email = :email",
                new MapSqlParameterSource("email", email.toLowerCase()), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public Optional<User> findByIdentifier(String identifier) {
        List<User> rows = jdbc.query(
                "SELECT " + COLS + " FROM users WHERE username = :ident OR email = :ident",
                new MapSqlParameterSource("ident", identifier.toLowerCase()), MAPPER);
        return rows.stream().findFirst();
    }

    @Override
    public boolean existsByUsername(String username) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE username = :username",
                new MapSqlParameterSource("username", username.toLowerCase()), Long.class);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByEmail(String email) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = :email",
                new MapSqlParameterSource("email", email.toLowerCase()), Long.class);
        return count != null && count > 0;
    }

    @Override
    public void save(User user) {
        jdbc.update(
                "INSERT INTO users (id, name, username, email, password_hash, avatar_url, bio, role, created_at) "
                        + "VALUES (:id, :name, :username, :email, :passwordHash, :avatarUrl, :bio, :role, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", user.getId())
                        .addValue("name", user.getName())
                        .addValue("username", user.getUsername().toLowerCase())
                        .addValue("email", user.getEmail().toLowerCase())
                        .addValue("passwordHash", user.getPasswordHash())
                        .addValue("avatarUrl", user.getAvatarUrl())
                        .addValue("bio", user.getBio() == null ? "" : user.getBio())
                        .addValue("role", user.getRole().name())
                        .addValue("createdAt", user.getCreatedAt()));
    }

    @Override
    public void updateProfile(String id, String name, String bio, String avatarUrl) {
        jdbc.update(
                "UPDATE users SET name = :name, bio = :bio, avatar_url = :avatarUrl WHERE id = :id",
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("name", name)
                        .addValue("bio", bio)
                        .addValue("avatarUrl", avatarUrl));
    }

    @Override
    public void updatePassword(String id, String passwordHash) {
        jdbc.update("UPDATE users SET password_hash = :hash WHERE id = :id",
                new MapSqlParameterSource().addValue("id", id).addValue("hash", passwordHash));
    }

    @Override
    public long countAll() {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM users",
                new MapSqlParameterSource(), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public List<User> findRecent(int limit) {
        return jdbc.query("SELECT " + COLS + " FROM users ORDER BY created_at DESC LIMIT :limit",
                new MapSqlParameterSource("limit", limit), MAPPER);
    }

    @Override
    public List<User> search(String query, int limit) {
        return jdbc.query(
                "SELECT " + COLS + " FROM users WHERE username LIKE :q OR name LIKE :q OR email LIKE :q "
                        + "ORDER BY created_at DESC LIMIT :limit",
                new MapSqlParameterSource().addValue("q", "%" + query + "%").addValue("limit", limit), MAPPER);
    }

    @Override
    public Map<String, User> findAllByIds(Collection<String> ids) {
        Map<String, User> out = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return out;
        }
        List<User> rows = jdbc.query("SELECT " + COLS + " FROM users WHERE id IN (:ids)",
                new MapSqlParameterSource("ids", ids), MAPPER);
        rows.forEach(user -> out.put(user.getId(), user));
        return out;
    }

    @Override
    public void delete(String id) {
        jdbc.update("DELETE FROM users WHERE id = :id", new MapSqlParameterSource("id", id));
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT " + COLS + " FROM users ORDER BY created_at DESC LIMIT 1000",
                new MapSqlParameterSource(), MAPPER);
    }
}

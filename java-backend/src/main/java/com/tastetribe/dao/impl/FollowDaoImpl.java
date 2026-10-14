package com.tastetribe.dao.impl;

import com.tastetribe.dao.FollowDao;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

/** JDBC implementation of {@link FollowDao} (community follow graph). */
@Repository
public class FollowDaoImpl implements FollowDao {

    private final NamedParameterJdbcTemplate jdbc;

    public FollowDaoImpl(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void follow(String followerId, String followingId) {
        jdbc.update("INSERT IGNORE INTO follows (follower_id, following_id, created_at) VALUES (:f, :t, :now)",
                new MapSqlParameterSource()
                        .addValue("f", followerId)
                        .addValue("t", followingId)
                        .addValue("now", java.time.LocalDateTime.now()));
    }

    @Override
    public void unfollow(String followerId, String followingId) {
        jdbc.update("DELETE FROM follows WHERE follower_id = :f AND following_id = :t",
                new MapSqlParameterSource().addValue("f", followerId).addValue("t", followingId));
    }

    @Override
    public boolean isFollowing(String followerId, String followingId) {
        Long count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM follows WHERE follower_id = :f AND following_id = :t",
                new MapSqlParameterSource().addValue("f", followerId).addValue("t", followingId), Long.class);
        return count != null && count > 0;
    }

    @Override
    public List<String> followingIds(String followerId) {
        return jdbc.queryForList("SELECT following_id FROM follows WHERE follower_id = :f",
                new MapSqlParameterSource("f", followerId), String.class);
    }

    @Override
    public List<String> followerIds(String followingId) {
        return jdbc.queryForList("SELECT follower_id FROM follows WHERE following_id = :t",
                new MapSqlParameterSource("t", followingId), String.class);
    }

    @Override
    public long countFollowers(String userId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM follows WHERE following_id = :t",
                new MapSqlParameterSource("t", userId), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public long countFollowing(String userId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM follows WHERE follower_id = :f",
                new MapSqlParameterSource("f", userId), Long.class);
        return count == null ? 0 : count;
    }

    @Override
    public void deleteAllForUser(String userId) {
        jdbc.update("DELETE FROM follows WHERE follower_id = :f OR following_id = :f",
                new MapSqlParameterSource("f", userId));
    }
}

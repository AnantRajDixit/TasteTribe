package com.tastetribe.dao;

/** Follows (community graph: who follows whom). */
public interface FollowDao {

    void follow(String followerId, String followingId);

    void unfollow(String followerId, String followingId);

    boolean isFollowing(String followerId, String followingId);

    java.util.List<String> followingIds(String followerId);

    java.util.List<String> followerIds(String followingId);

    long countFollowers(String userId);

    long countFollowing(String userId);

    /** Remove every follow row touching a user (cascade on account delete). */
    void deleteAllForUser(String userId);
}

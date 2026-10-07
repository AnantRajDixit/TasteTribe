package com.tastetribe.model;

import java.time.LocalDateTime;

/**
 * A user's review of a recipe: a 1-5 star rating plus optional written comment.
 * One review per user per recipe (unique constraint) — updatable, never spam-able.
 * The username/avatar are joined at read time (transient display fields).
 */
public class Review extends BaseEntity {

    private String recipeId;
    private String userId;
    private int rating;
    private String comment;
    private LocalDateTime updatedAt;
    private String username;
    private String name;
    private String avatarUrl;

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

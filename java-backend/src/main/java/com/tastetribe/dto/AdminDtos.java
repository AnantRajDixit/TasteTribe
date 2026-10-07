package com.tastetribe.dto;

import java.time.Instant;
import java.util.List;

/** Admin dashboard DTOs (platform stats, moderation rows). */
public final class AdminDtos {

    private AdminDtos() {
    }

    public record AdminUserRow(String id, String name, String username, String email, String role,
                               long recipesCount, Instant createdAt) {
    }

    public record AdminRecipeRow(String id, String title, String authorUsername, String status,
                                 int views, Double avgRating) {
    }

    public record AdminCommentRow(String id, String text, String username, String recipeTitle, Instant createdAt) {
    }

    public record AdminReportRow(String id, String targetType, String targetId, String reason,
                                 String reporterUsername, String status, String targetTitle, Instant createdAt) {
    }

    public record StatsResponse(long users, long recipes, long publishedRecipes, long reviews,
                                long comments, long pendingReports,
                                List<AdminUserRow> recentUsers, List<AdminRecipeRow> topRecipes) {
    }
}

package com.tastetribe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Social interaction DTOs: reviews, comments, reports, follows, like/save toggles. */
public final class SocialDtos {

    private SocialDtos() {
    }

    public record ReviewRequest(@Min(1) @Max(5) int rating, @Size(max = 2000) String comment) {
    }

    public record ReviewResponse(String id, String recipeId, String userId, String username, String name,
                                 String avatarUrl, int rating, String comment,
                                 Instant createdAt, Instant updatedAt) {

        public static ReviewResponse from(com.tastetribe.model.Review review) {
            return new ReviewResponse(review.getId(), review.getRecipeId(), review.getUserId(),
                    review.getUsername(), review.getName(), review.getAvatarUrl(), review.getRating(),
                    review.getComment(), toInstant(review.getCreatedAt()), toInstant(review.getUpdatedAt()));
        }
    }

    public record CommentRequest(@NotBlank @Size(max = 1000) String text) {
    }

    public record CommentResponse(String id, String recipeId, String userId, String username, String name,
                                  String avatarUrl, String text, Instant createdAt) {

        public static CommentResponse from(com.tastetribe.model.Comment comment) {
            return new CommentResponse(comment.getId(), comment.getRecipeId(), comment.getUserId(),
                    comment.getUsername(), comment.getName(), comment.getAvatarUrl(), comment.getText(),
                    toInstant(comment.getCreatedAt()));
        }
    }

    /** Result of a like/favorite toggle. */
    public record ToggleResponse(boolean active, long count) {
    }

    public record FollowActionResponse(boolean following, long followersCount) {
    }

    public record ReportRequest(@NotBlank String targetType, @NotBlank String targetId,
                                @NotBlank @Size(min = 3, max = 500) String reason) {
    }

    private static Instant toInstant(java.time.LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

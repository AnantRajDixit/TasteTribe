package com.tastetribe.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Authentication & profile DTOs (records — immutable data carriers over the wire).
 * Kept strictly separate from entities so the password hash can never leak into JSON.
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 2, max = 80) String name,
            @NotBlank @Pattern(regexp = "^[a-zA-Z0-9_]{3,30}$", message = "3-30 letters, digits or underscore") String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 128) String password,
            String avatarUrl) {
    }

    public record LoginRequest(@NotBlank String usernameOrEmail, @NotBlank String password) {
    }

    public record UpdateProfileRequest(
            @NotBlank @Size(min = 2, max = 80) String name,
            @Size(max = 400) String bio,
            String avatarUrl) {
    }

    public record PasswordChangeRequest(@NotBlank String currentPassword,
                                        @NotBlank @Size(min = 8, max = 128) String newPassword) {
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    public record ResetPasswordRequest(@NotBlank String token,
                                       @NotBlank @Size(min = 8, max = 128) String newPassword) {
    }

    /** Public view of a user (never contains the password hash). */
    public record UserResponse(String id, String name, String username, String email, String avatarUrl,
                               String bio, String role, Instant createdAt) {

        public static UserResponse from(com.tastetribe.model.User user, boolean includeEmail) {
            return new UserResponse(
                    user.getId(),
                    user.getName(),
                    user.getUsername(),
                    includeEmail ? user.getEmail() : null,
                    user.getAvatarUrl(),
                    user.getBio(),
                    user.getRole().json(),
                    toInstant(user.getCreatedAt()));
        }
    }

    /** Profile view = user + community counters. */
    public record ProfileResponse(String id, String name, String username, String email, String avatarUrl,
                                  String bio, String role, Instant createdAt,
                                  long followersCount, long followingCount, long recipesCount,
                                  Double avgRating, boolean isFollowing) {
    }

    public record FollowResponse(boolean following, long followersCount) {
    }

    /** Demo environment has no mail provider — the reset token is returned directly. */
    public record ForgotPasswordResponse(boolean ok, String resetToken, String message) {
    }

    private static Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

package com.tastetribe.dto;

import com.tastetribe.model.Difficulty;
import com.tastetribe.model.Nutrition;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.RecipeStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

/** Recipe DTOs: create/update request, full response, paginated list, serving scaler. */
public final class RecipeDtos {

    private RecipeDtos() {
    }

    public record IngredientRequest(
            @NotBlank @Size(max = 120) String name,
            @Min(1) double quantity,
            String unit,
            boolean optional) {
    }

    public record NutritionRequest(Double calories, Double protein, Double carbs, Double fat) {
    }

    public record RecipeRequest(
            @NotBlank @Size(min = 3, max = 140) String title,
            @Size(max = 2000) String description,
            String coverImage,
            @NotBlank String cuisine,
            @NotBlank String category,
            @NotBlank String difficulty,
            String dietary,
            @Min(0) int prepTime,
            @Min(0) int cookTime,
            @Min(1) @jakarta.validation.constraints.Max(50) int servings,
            @NotEmpty List<IngredientRequest> ingredients,
            @NotEmpty List<@NotBlank String> instructions,
            NutritionRequest nutrition,
            List<String> tags,
            String status) {
    }

    /** Full recipe response — the mirror of the TS interface on the frontend. */
    public record RecipeResponse(
            String id, String title, String description, String coverImage,
            String cuisine, String category, String difficulty, String dietary,
            int prepTime, int cookTime, int totalTime, int servings,
            List<IngredientLine> ingredients, List<String> instructions, Nutrition nutrition,
            List<String> tags,
            String authorId, String authorName, String authorUsername,
            String status, String source,
            int views, int likesCount, int favoritesCount,
            Double avgRating, int ratingsCount,
            boolean likedByMe, boolean favoritedByMe,
            Instant createdAt, Instant updatedAt) {

        public record IngredientLine(String name, double quantity, String unit, boolean optional) {
        }

        public static RecipeResponse from(Recipe recipe, boolean likedByMe, boolean favoritedByMe) {
            List<IngredientLine> lines = recipe.getIngredients().stream()
                    .map(RecipeResponse::toLine)
                    .toList();
            return new RecipeResponse(
                    recipe.getId(), recipe.getTitle(), recipe.getDescription(), recipe.getCoverImage(),
                    recipe.getCuisine(), recipe.getCategory(),
                    recipe.getDifficulty() == null ? "Easy" : recipe.getDifficulty().getLabel(),
                    recipe.getDietary(),
                    recipe.getPrepTime(), recipe.getCookTime(), recipe.getTotalTime(), recipe.getServings(),
                    lines, recipe.getInstructions(), recipe.getNutrition(),
                    recipe.getTags(),
                    recipe.getAuthorId(), recipe.getAuthorName(), recipe.getAuthorUsername(),
                    recipe.getStatus() == null ? "published" : recipe.getStatus().json(),
                    recipe.getSource(),
                    recipe.getViews(), recipe.getLikesCount(), recipe.getFavoritesCount(),
                    recipe.getAvgRating() == null ? null : Math.round(recipe.getAvgRating() * 100.0) / 100.0,
                    recipe.getRatingsCount(),
                    likedByMe, favoritedByMe,
                    toInstant(recipe.getCreatedAt()), toInstant(recipe.getUpdatedAt()));
        }

        private static IngredientLine toLine(RecipeIngredient ingredient) {
            return new IngredientLine(ingredient.getName(), ingredient.getQuantity(),
                    ingredient.getUnit(), ingredient.isOptional());
        }
    }

    /** Generic pagination envelope. */
    public record RecipePage(List<RecipeResponse> items, long total, int page, int pages) {
    }

    public record ScaledIngredientResponse(String name, double quantity, String unit, boolean optional,
                                           String display) {
    }

    public record ScaleResponse(String recipeId, int baseServings, int servings,
                                List<ScaledIngredientResponse> ingredients) {
    }

    public record CategoryResponse(String id, String name, String slug, String description,
                                   String imageUrl, long recipesCount) {
    }

    public record CategoryRequest(@NotBlank @Size(min = 2, max = 60) String name,
                                  @Size(max = 300) String description, String imageUrl) {
    }

    private static Instant toInstant(java.time.LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}

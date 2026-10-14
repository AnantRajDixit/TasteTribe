package com.tastetribe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/** AI assistant DTOs: chat, recipe generation, ingredient substitution. */
public final class AiDtos {

    private AiDtos() {
    }

    public record ChatRequest(@NotBlank @Size(max = 2000) String message,
                              @NotBlank String sessionId,
                              String recipeId) {
    }

    public record ChatResponse(String reply) {
    }

    public record GenerateRecipeRequest(@Size(min = 1) List<@NotBlank String> ingredients,
                                        Integer maxTime, String difficulty, Integer servings,
                                        String mealType) {
    }

    /** Structured recipe the AI produced — same shape as RecipeRequest so the UI can save it. */
    public record GeneratedRecipeResponse(com.tastetribe.dto.RecipeDtos.RecipeRequest recipe) {
    }

    public record SubstituteRequest(@NotBlank @Size(min = 2, max = 120) String ingredient,
                                    String recipeId) {
    }

    public record SubstituteOption(String name, String ratio, String taste, String texture,
                                   String temperature, String quantity) {
    }

    public record SubstituteResponse(String ingredient, String summary, List<SubstituteOption> substitutes) {
    }
}

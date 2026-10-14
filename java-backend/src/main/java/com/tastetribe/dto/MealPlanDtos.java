package com.tastetribe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.List;

/** Weekly meal planner DTOs. */
public final class MealPlanDtos {

    private MealPlanDtos() {
    }

    /** Slot values accepted by the API (kept lowercase on the wire). */
    public static final String SLOT_PATTERN = "^(breakfast|lunch|dinner)$";

    public record MealPlanEntryResponse(String id, String recipeId, String recipeTitle,
                                        String recipeImage, int recipeTotalTime,
                                        String planDate, String slot, int servings) {
    }

    public record MealPlanResponse(String weekStart, List<MealPlanEntryResponse> entries) {
    }

    public record AddMealRequest(@NotBlank String recipeId,
                                 @NotBlank String planDate,
                                 @NotBlank @Pattern(regexp = SLOT_PATTERN) String slot,
                                 @Min(1) @Max(50) Integer servings) {
    }

    /** Drag-and-drop move: the entry keeps its recipe but changes date/slot. */
    public record MoveMealRequest(@NotBlank String planDate,
                                  @NotBlank @Pattern(regexp = SLOT_PATTERN) String slot) {
    }
}

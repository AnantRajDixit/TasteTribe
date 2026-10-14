package com.tastetribe.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/** Shopping-list DTOs. */
public final class ShoppingDtos {

    private ShoppingDtos() {
    }

    public record ShoppingItemResponse(String id, String name, double quantity, String unit,
                                       boolean checked, String recipeTitle) {
    }

    public record ShoppingListResponse(List<ShoppingItemResponse> items) {
    }

    public record AddItemRequest(@NotBlank @Size(max = 120) String name,
                                 @Min(1) double quantity, String unit) {
    }

    public record AddRecipeRequest(@NotBlank String recipeId, @Min(1) @Max(50) Integer servings) {
    }

    public record ToggleItemRequest(@NotNull Boolean checked) {
    }
}

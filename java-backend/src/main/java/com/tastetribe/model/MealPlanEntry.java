package com.tastetribe.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * One planned meal: a recipe assigned to a date and a slot (breakfast/lunch/dinner)
 * for a specific user. The recipe title/image are joined in at read time for display.
 */
public class MealPlanEntry extends BaseEntity {

    private String userId;
    private String recipeId;
    private LocalDate planDate;
    private String slot;
    private int servings;
    private String recipeTitle;
    private String recipeImage;
    private int recipeTotalTime;

    public MealPlanEntry() {
        super();
    }

    public MealPlanEntry(String id, LocalDateTime createdAt) {
        super(id, createdAt);
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public LocalDate getPlanDate() {
        return planDate;
    }

    public void setPlanDate(LocalDate planDate) {
        this.planDate = planDate;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public String getRecipeImage() {
        return recipeImage;
    }

    public void setRecipeImage(String recipeImage) {
        this.recipeImage = recipeImage;
    }

    public int getRecipeTotalTime() {
        return recipeTotalTime;
    }

    public void setRecipeTotalTime(int recipeTotalTime) {
        this.recipeTotalTime = recipeTotalTime;
    }
}

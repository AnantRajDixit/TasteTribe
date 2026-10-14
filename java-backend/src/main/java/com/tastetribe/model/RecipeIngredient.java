package com.tastetribe.model;

/**
 * One structured ingredient line of a recipe (name + quantity + unit + optional flag).
 * Stored in the normalized {@code recipe_ingredients} table — never as one big text field.
 */
public class RecipeIngredient {

    private long id;
    private String recipeId;
    private String name;
    private double quantity;
    private String unit;
    private boolean optional;
    private int position;

    public RecipeIngredient() {
    }

    public RecipeIngredient(String name, double quantity, String unit, boolean optional) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.optional = optional;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

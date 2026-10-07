package com.tastetribe.model;

/** One line on a user's shopping list (merged across recipes, quantity-aware). */
public class ShoppingItem {

    private String id;
    private String userId;
    private String name;
    private double quantity;
    private String unit;
    private boolean checked;
    private String recipeTitle;
    private int position;

    public ShoppingItem() {
    }

    public ShoppingItem(String id, String userId, String name, double quantity, String unit,
                        boolean checked, String recipeTitle, int position) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.checked = checked;
        this.recipeTitle = recipeTitle;
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getRecipeTitle() {
        return recipeTitle;
    }

    public void setRecipeTitle(String recipeTitle) {
        this.recipeTitle = recipeTitle;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

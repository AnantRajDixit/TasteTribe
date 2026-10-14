package com.tastetribe.model;

/** Nutritional information per serving (all values nullable). */
public class Nutrition {

    private Double calories;
    private Double protein;
    private Double carbs;
    private Double fat;

    public Nutrition() {
    }

    public Nutrition(Double calories, Double protein, Double carbs, Double fat) {
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getProtein() {
        return protein;
    }

    public void setProtein(Double protein) {
        this.protein = protein;
    }

    public Double getCarbs() {
        return carbs;
    }

    public void setCarbs(Double carbs) {
        this.carbs = carbs;
    }

    public Double getFat() {
        return fat;
    }

    public void setFat(Double fat) {
        this.fat = fat;
    }
}

package com.tastetribe.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Draft / published lifecycle of a recipe. */
public enum RecipeStatus {
    DRAFT, PUBLISHED;

    @JsonValue
    public String json() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static RecipeStatus from(String value) {
        if (value == null) {
            return PUBLISHED;
        }
        for (RecipeStatus s : values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}

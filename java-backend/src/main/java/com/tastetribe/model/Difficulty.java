package com.tastetribe.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Recipe difficulty levels. The database stores the human label ("Easy", ...)
 * while the enum gives type safety in Java code — a classic enum use case.
 */
public enum Difficulty {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard");

    private final String label;

    Difficulty(String label) {
        this.label = label;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static Difficulty from(String value) {
        if (value == null) {
            return EASY;
        }
        for (Difficulty d : values()) {
            if (d.label.equalsIgnoreCase(value) || d.name().equalsIgnoreCase(value)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Unknown difficulty: " + value);
    }
}

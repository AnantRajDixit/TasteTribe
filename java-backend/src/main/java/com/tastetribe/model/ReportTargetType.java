package com.tastetribe.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** What a report points at. */
public enum ReportTargetType {
    RECIPE, COMMENT;

    @JsonValue
    public String json() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static ReportTargetType from(String value) {
        return ReportTargetType.valueOf(value.toUpperCase());
    }
}

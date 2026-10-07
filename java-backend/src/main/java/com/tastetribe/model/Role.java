package com.tastetribe.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Platform roles. Serialized lowercase in JSON ("user" / "admin"). */
public enum Role {
    USER, ADMIN;

    @JsonValue
    public String json() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role from(String value) {
        return value == null ? USER : Role.valueOf(value.toUpperCase());
    }
}

package com.tastetribe.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tastetribe.model.Nutrition;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Small JSON bridge for columns that store JSON documents (tags, instruction steps,
 * nutrition). Centralises (de)serialisation so DAOs stay clean — and gives one place
 * to demonstrate Jackson generics ({@link TypeReference}).
 */
@Component
public class JsonSupport {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    public String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException("JSON serialisation failed", ex);
        }
    }

    public List<String> toStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, STRING_LIST);
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    public Nutrition toNutrition(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(json, Nutrition.class);
        } catch (Exception ex) {
            return null;
        }
    }
}

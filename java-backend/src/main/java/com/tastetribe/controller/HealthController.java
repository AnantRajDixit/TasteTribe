package com.tastetribe.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Health / identity endpoint so the frontend can confirm the API is reachable. */
@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of(
                "message", "TasteTribe API",
                "status", "ok",
                "backend", "Java 17 / Spring Boot / JDBC");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok");
    }
}

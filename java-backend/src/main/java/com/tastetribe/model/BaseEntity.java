package com.tastetribe.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity: every persistent object gets a string UUID primary key (never a
 * database-generated integer that leaks row counts) and a creation timestamp.
 * Demonstrates inheritance — all entities extend this class.
 */
public abstract class BaseEntity {

    private String id;
    private LocalDateTime createdAt;

    protected BaseEntity() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }

    protected BaseEntity(String id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

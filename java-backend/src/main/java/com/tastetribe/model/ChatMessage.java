package com.tastetribe.model;

import java.time.LocalDateTime;

/** One message of an AI assistant conversation (persisted so chats survive reloads). */
public class ChatMessage {

    private String id;
    private String userId;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime createdAt;

    public ChatMessage() {
    }

    public ChatMessage(String id, String userId, String sessionId, String role, String content,
                       LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.sessionId = sessionId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

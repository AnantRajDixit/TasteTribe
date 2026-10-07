package com.tastetribe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tastetribe.exception.AppException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Thin client for the Emergent LLM gateway (an OpenAI-compatible
 * {@code /chat/completions} endpoint).
 *
 * <p>The API key is read from the server-side configuration only and never reaches the
 * browser — all AI traffic is proxied through this backend service.</p>
 */
@Component
public class LlmClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String appId;

    public LlmClient(@Value("${app.ai.api-key:}") String apiKey,
                     @Value("${app.ai.base-url}") String baseUrl,
                     @Value("${app.ai.model:gpt-5.4}") String model,
                     @Value("${app.ai.app-id:}") String appId) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.model = model;
        this.appId = appId;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Send a conversation and return the assistant's text.
     *
     * @param messages    role/content pairs, system message first
     * @param jsonMode    request a strict JSON object back
     */
    public String complete(List<Map<String, String>> messages, boolean jsonMode) {
        if (!isConfigured()) {
            throw new AppException(503, "The AI assistant is not configured on this server.");
        }
        try {
            ObjectNode payload = MAPPER.createObjectNode();
            payload.put("model", model);
            ArrayNode array = payload.putArray("messages");
            for (Map<String, String> message : messages) {
                ObjectNode node = array.addObject();
                node.put("role", message.get("role"));
                node.put("content", message.get("content"));
            }
            if (jsonMode) {
                payload.putObject("response_format").put("type", "json_object");
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .timeout(Duration.ofSeconds(90))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("X-App-ID", appId == null ? "" : appId)
                    .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new AppException(502, "The AI assistant is unavailable right now. Please try again.");
            }
            JsonNode root = MAPPER.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().isBlank()) {
                throw new AppException(502, "The AI assistant returned an empty answer.");
            }
            return content.asText();
        } catch (AppException ex) {
            throw ex;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new AppException(502, "The AI request was interrupted. Please try again.");
        } catch (Exception ex) {
            // Never surface driver/HTTP internals to the client.
            throw new AppException(502, "The AI assistant is unavailable right now. Please try again.");
        }
    }

    /** Parse a JSON object answer, tolerating markdown code fences. */
    public JsonNode parseJson(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```(?:json)?", "").replaceAll("```$", "").trim();
        }
        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        try {
            return MAPPER.readTree(cleaned);
        } catch (Exception ex) {
            throw new AppException(502, "The AI assistant returned an unexpected format. Please try again.");
        }
    }
}

package com.tastetribe.controller;

import com.tastetribe.dto.AiDtos.ChatRequest;
import com.tastetribe.dto.AiDtos.ChatResponse;
import com.tastetribe.dto.AiDtos.GenerateRecipeRequest;
import com.tastetribe.dto.AiDtos.SubstituteRequest;
import com.tastetribe.dto.AiDtos.SubstituteResponse;
import com.tastetribe.dto.RecipeDtos.RecipeRequest;
import com.tastetribe.model.ChatMessage;
import com.tastetribe.service.AiAssistantService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI Cooking Assistant endpoints — {@code /api/ai/*}.
 *
 * <p>Security: the LLM API key lives only in server configuration. The browser never
 * sees it; every AI call is proxied through this controller.</p>
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiAssistantService aiService;
    private final SessionContext session;

    public AiController(AiAssistantService aiService, SessionContext session) {
        this.aiService = aiService;
        this.session = session;
    }

    /** Lets the UI show/hide the assistant gracefully when no key is configured. */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of("configured", aiService.isConfigured());
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest body, HttpServletRequest request) {
        return aiService.chat(session.require(request), body);
    }

    @GetMapping("/history")
    public List<Map<String, Object>> history(@RequestParam String sessionId, HttpServletRequest request) {
        return aiService.history(session.require(request), sessionId).stream()
                .map(message -> Map.<String, Object>of(
                        "id", message.getId(),
                        "role", message.getRole(),
                        "content", message.getContent()))
                .toList();
    }

    /** Generate a full structured recipe from pantry ingredients + constraints. */
    @PostMapping("/generate-recipe")
    public RecipeRequest generateRecipe(@Valid @RequestBody GenerateRecipeRequest body,
                                        HttpServletRequest request) {
        session.require(request);
        return aiService.generateRecipe(body);
    }

    /** Ingredient substitution with taste / texture / temperature / quantity guidance. */
    @PostMapping("/substitute")
    public SubstituteResponse substitute(@Valid @RequestBody SubstituteRequest body,
                                         HttpServletRequest request) {
        session.require(request);
        return aiService.substitute(body);
    }
}

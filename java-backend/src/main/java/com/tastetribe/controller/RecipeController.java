package com.tastetribe.controller;

import com.tastetribe.dao.RecipeQuery;
import com.tastetribe.dto.RecipeDtos.RecipePage;
import com.tastetribe.dto.RecipeDtos.RecipeRequest;
import com.tastetribe.dto.RecipeDtos.RecipeResponse;
import com.tastetribe.dto.RecipeDtos.ScaleResponse;
import com.tastetribe.dto.SocialDtos.ToggleResponse;
import com.tastetribe.model.Difficulty;
import com.tastetribe.model.RecipeStatus;
import com.tastetribe.model.User;
import com.tastetribe.service.RecipeService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recipe endpoints: discovery search, detail, CRUD, serving scaler, like/save toggles
 * and the personalized feed.
 */
@RestController
public class RecipeController {

    private final RecipeService recipeService;
    private final SessionContext session;

    public RecipeController(RecipeService recipeService, SessionContext session) {
        this.recipeService = recipeService;
        this.session = session;
    }

    /** Powerful discovery search: name, ingredient, cuisine, category, difficulty, diet, tag, time. */
    @GetMapping("/recipes")
    public RecipePage list(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String ingredient,
                           @RequestParam(required = false) String cuisine,
                           @RequestParam(required = false) String category,
                           @RequestParam(required = false) String difficulty,
                           @RequestParam(required = false) String dietary,
                           @RequestParam(required = false) String tag,
                           @RequestParam(required = false) Integer maxTime,
                           @RequestParam(defaultValue = "newest") String sort,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "12") int limit,
                           HttpServletRequest request) {
        RecipeQuery query = new RecipeQuery(q, ingredient, cuisine, category,
                parseDifficulty(difficulty), dietary, tag, maxTime, RecipeStatus.PUBLISHED);
        return recipeService.search(query, sort, Math.max(1, page), clamp(limit), currentUser(request));
    }

    @GetMapping("/feed")
    public RecipePage feed(@RequestParam(defaultValue = "latest") String tab,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "12") int limit,
                           HttpServletRequest request) {
        return recipeService.feed(tab, Math.max(1, page), clamp(limit), currentUser(request));
    }

    @GetMapping("/recipes/{id}")
    public RecipeResponse get(@PathVariable String id, HttpServletRequest request) {
        return recipeService.get(id, currentUser(request));
    }

    /** Serving scaler — all arithmetic happens in the Java service layer. */
    @GetMapping("/recipes/{id}/scale")
    public ScaleResponse scale(@PathVariable String id, @RequestParam int servings) {
        int safe = Math.max(1, Math.min(50, servings));
        return recipeService.scale(id, safe);
    }

    @PostMapping("/recipes")
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeRequest body,
                                                 HttpServletRequest request) {
        User user = session.require(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.create(user, body));
    }

    @PatchMapping("/recipes/{id}")
    public RecipeResponse update(@PathVariable String id, @Valid @RequestBody RecipeRequest body,
                                 HttpServletRequest request) {
        return recipeService.update(id, session.require(request), body);
    }

    @DeleteMapping("/recipes/{id}")
    public Map<String, Object> delete(@PathVariable String id, HttpServletRequest request) {
        recipeService.delete(id, session.require(request));
        return Map.of("ok", true);
    }

    @PostMapping("/recipes/{id}/like")
    public ToggleResponse like(@PathVariable String id, HttpServletRequest request) {
        return recipeService.toggleLike(id, session.require(request));
    }

    @PostMapping("/recipes/{id}/favorite")
    public ToggleResponse favorite(@PathVariable String id, HttpServletRequest request) {
        return recipeService.toggleFavorite(id, session.require(request));
    }

    private User currentUser(HttpServletRequest request) {
        return session.current(request).orElse(null);
    }

    private static int clamp(int limit) {
        return Math.max(1, Math.min(48, limit));
    }

    private static Difficulty parseDifficulty(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Difficulty.from(value);
        } catch (IllegalArgumentException ex) {
            return null; // unknown filter value simply does not filter
        }
    }
}

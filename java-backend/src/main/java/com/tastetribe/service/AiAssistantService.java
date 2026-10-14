package com.tastetribe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.tastetribe.dao.ChatDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dto.AiDtos.ChatRequest;
import com.tastetribe.dto.AiDtos.ChatResponse;
import com.tastetribe.dto.AiDtos.GenerateRecipeRequest;
import com.tastetribe.dto.AiDtos.SubstituteOption;
import com.tastetribe.dto.AiDtos.SubstituteRequest;
import com.tastetribe.dto.AiDtos.SubstituteResponse;
import com.tastetribe.dto.RecipeDtos.IngredientRequest;
import com.tastetribe.dto.RecipeDtos.NutritionRequest;
import com.tastetribe.dto.RecipeDtos.RecipeRequest;
import com.tastetribe.model.ChatMessage;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * The AI Cooking Assistant. Three capabilities, all executed server-side:
 * <ol>
 *   <li><b>Chat</b> — cooking Q&amp;A with optional recipe context and persisted history;</li>
 *   <li><b>Recipe generation</b> — structured recipe from pantry ingredients + constraints;</li>
 *   <li><b>Ingredient substitution</b> — alternatives with taste/texture/temperature/quantity notes.</li>
 * </ol>
 */
@Service
public class AiAssistantService {

    private static final int HISTORY_LIMIT = 12;

    private static final String CHAT_SYSTEM = """
            You are TasteTribe's friendly, expert sous-chef. Help home cooks with practical
            cooking guidance: what to cook from the ingredients they have, techniques,
            substitutions, scaling, storage and food safety.
            Keep answers tight and useful: 120 words or fewer unless the user asks for a full
            recipe. Use short markdown bullet lists for steps or options. Never invent
            unsafe advice; mention food-safety caveats for meat, eggs and reheating.
            """;

    private final LlmClient llm;
    private final ChatDao chatDao;
    private final RecipeDao recipeDao;

    public AiAssistantService(LlmClient llm, ChatDao chatDao, RecipeDao recipeDao) {
        this.llm = llm;
        this.chatDao = chatDao;
        this.recipeDao = recipeDao;
    }

    public boolean isConfigured() {
        return llm.isConfigured();
    }

    // ---------- 1. Chat ----------

    public ChatResponse chat(User user, ChatRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", CHAT_SYSTEM + recipeContext(request.recipeId())));

        // Replay persisted history so the conversation has memory across reloads.
        chatDao.findRecent(request.sessionId(), user.getId(), HISTORY_LIMIT)
                .forEach(message -> messages.add(Map.of(
                        "role", message.getRole(),
                        "content", message.getContent())));
        messages.add(Map.of("role", "user", "content", request.message()));

        String reply = llm.complete(messages, false);

        persist(user, request.sessionId(), "user", request.message());
        persist(user, request.sessionId(), "assistant", reply);
        return new ChatResponse(reply);
    }

    public List<ChatMessage> history(User user, String sessionId) {
        return chatDao.findRecent(sessionId, user.getId(), 50);
    }

    /** Injects the recipe the user is currently looking at, so answers stay on-topic. */
    private String recipeContext(String recipeId) {
        if (recipeId == null || recipeId.isBlank()) {
            return "";
        }
        Optional<Recipe> found = recipeDao.findById(recipeId);
        if (found.isEmpty()) {
            return "";
        }
        Recipe recipe = found.get();
        String ingredients = recipe.getIngredients().stream()
                .map(ing -> ing.getQuantity() + " " + ing.getUnit() + " " + ing.getName())
                .collect(Collectors.joining(", "));
        return """

                The user is currently viewing this recipe — use it as context when relevant:
                Title: %s
                Cuisine: %s | Category: %s | Difficulty: %s
                Serves: %d | Total time: %d minutes
                Ingredients: %s
                """.formatted(recipe.getTitle(), recipe.getCuisine(), recipe.getCategory(),
                recipe.getDifficulty().getLabel(), recipe.getServings(), recipe.getTotalTime(), ingredients);
    }

    private void persist(User user, String sessionId, String role, String content) {
        chatDao.save(new ChatMessage(UUID.randomUUID().toString(), user.getId(), sessionId,
                role, content, LocalDateTime.now()));
    }

    // ---------- 2. Recipe generation ----------

    public RecipeRequest generateRecipe(GenerateRecipeRequest request) {
        String prompt = """
                Create ONE achievable recipe using mainly these ingredients: %s.
                %s%s%s
                Respond with ONLY a JSON object in exactly this shape:
                {
                  "title": "string",
                  "description": "one or two appetising sentences",
                  "cuisine": "Indian|Italian|Chinese|Mexican|Korean|Continental|American|Thai|Japanese",
                  "category": "Breakfast|Lunch|Dinner|Snacks|Desserts|Beverages|Quick Meals|High Protein",
                  "difficulty": "Easy|Medium|Hard",
                  "dietary": "Vegetarian|Non-Vegetarian|Vegan",
                  "prepTime": 10,
                  "cookTime": 20,
                  "servings": 2,
                  "ingredients": [{"name":"Onion","quantity":2,"unit":"pieces","optional":false}],
                  "instructions": ["Step one...", "Step two..."],
                  "nutrition": {"calories":420,"protein":28,"carbs":35,"fat":16},
                  "tags": ["quick","one-pan"]
                }
                Rules: quantity must be a positive number; unit is a short string (g, ml, tsp, tbsp,
                cup, pieces, cloves); 4-10 instruction steps, each a complete sentence; include
                basic pantry items (oil, salt, spices) even if not listed by the user.
                """.formatted(
                String.join(", ", request.ingredients()),
                request.maxTime() == null ? "" : "Total time must be at most " + request.maxTime() + " minutes.\n",
                request.difficulty() == null ? "" : "Difficulty should be " + request.difficulty() + ".\n",
                request.servings() == null ? "" : "It should serve " + request.servings() + " people.\n");

        String raw = llm.complete(List.of(
                Map.of("role", "system", "content",
                        "You are a precise recipe generator. You reply with strict JSON only, no prose."),
                Map.of("role", "user", "content", prompt)), true);

        JsonNode json = llm.parseJson(raw);
        return toRecipeRequest(json, request);
    }

    private RecipeRequest toRecipeRequest(JsonNode json, GenerateRecipeRequest request) {
        List<IngredientRequest> ingredients = new ArrayList<>();
        for (JsonNode node : json.path("ingredients")) {
            double quantity = node.path("quantity").asDouble(1);
            ingredients.add(new IngredientRequest(
                    node.path("name").asText("Ingredient"),
                    quantity <= 0 ? 1 : quantity,
                    node.path("unit").asText("piece"),
                    node.path("optional").asBoolean(false)));
        }
        if (ingredients.isEmpty()) {
            ingredients.add(new IngredientRequest(request.ingredients().get(0), 1, "piece", false));
        }

        List<String> instructions = new ArrayList<>();
        json.path("instructions").forEach(node -> {
            String step = node.asText("").trim();
            if (!step.isEmpty()) {
                instructions.add(step);
            }
        });
        if (instructions.isEmpty()) {
            instructions.add("Combine the ingredients and cook until done.");
        }

        List<String> tags = new ArrayList<>();
        json.path("tags").forEach(node -> tags.add(node.asText()));
        tags.add("ai-generated");

        JsonNode nutritionNode = json.path("nutrition");
        NutritionRequest nutrition = nutritionNode.isMissingNode() ? null : new NutritionRequest(
                nutritionNode.path("calories").isNumber() ? nutritionNode.path("calories").asDouble() : null,
                nutritionNode.path("protein").isNumber() ? nutritionNode.path("protein").asDouble() : null,
                nutritionNode.path("carbs").isNumber() ? nutritionNode.path("carbs").asDouble() : null,
                nutritionNode.path("fat").isNumber() ? nutritionNode.path("fat").asDouble() : null);

        int prep = Math.max(0, json.path("prepTime").asInt(10));
        int cook = Math.max(0, json.path("cookTime").asInt(20));
        int servings = json.path("servings").asInt(request.servings() == null ? 2 : request.servings());

        return new RecipeRequest(
                json.path("title").asText("AI Kitchen Creation"),
                json.path("description").asText(""),
                null,
                json.path("cuisine").asText("Continental"),
                json.path("category").asText("Dinner"),
                json.path("difficulty").asText(request.difficulty() == null ? "Easy" : request.difficulty()),
                json.path("dietary").asText("Non-Vegetarian"),
                prep,
                cook,
                Math.max(1, Math.min(50, servings)),
                ingredients,
                instructions,
                nutrition,
                tags,
                "draft");
    }

    // ---------- 3. Ingredient substitution ----------

    public SubstituteResponse substitute(SubstituteRequest request) {
        String prompt = """
                The cook has run out of: %s.%s
                Suggest 3 to 4 realistic substitutes. Respond with ONLY this JSON shape:
                {
                  "summary": "one sentence overview",
                  "substitutes": [
                    {
                      "name": "Olive oil",
                      "ratio": "1:1",
                      "taste": "how the flavour changes",
                      "texture": "how texture/structure changes",
                      "temperature": "any cooking-temperature caveat",
                      "quantity": "exact amount to use instead"
                    }
                  ]
                }
                Be specific and practical for home kitchens.
                """.formatted(request.ingredient(), recipeContext(request.recipeId()));

        JsonNode json = llm.parseJson(llm.complete(List.of(
                Map.of("role", "system", "content",
                        "You are a culinary science expert. You reply with strict JSON only."),
                Map.of("role", "user", "content", prompt)), true));

        List<SubstituteOption> options = new ArrayList<>();
        for (JsonNode node : json.path("substitutes")) {
            options.add(new SubstituteOption(
                    node.path("name").asText(""),
                    node.path("ratio").asText(""),
                    node.path("taste").asText(""),
                    node.path("texture").asText(""),
                    node.path("temperature").asText(""),
                    node.path("quantity").asText("")));
        }
        return new SubstituteResponse(request.ingredient(), json.path("summary").asText(""), options);
    }
}

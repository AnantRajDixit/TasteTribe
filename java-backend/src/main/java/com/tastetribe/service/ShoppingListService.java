package com.tastetribe.service;

import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.ShoppingListDao;
import com.tastetribe.dto.ShoppingDtos.AddItemRequest;
import com.tastetribe.dto.ShoppingDtos.AddRecipeRequest;
import com.tastetribe.dto.ShoppingDtos.ShoppingItemResponse;
import com.tastetribe.dto.ShoppingDtos.ShoppingListResponse;
import com.tastetribe.dto.ShoppingDtos.ToggleItemRequest;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.ShoppingItem;
import com.tastetribe.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Shopping-list business logic. Adding a recipe merges its (optionally re-scaled)
 * ingredients into the list — identical name+unit lines are combined by summing
 * quantities. Quantities are computed by the BACKEND (recipe scaling business logic),
 * never trusted from the client.
 */
@Service
public class ShoppingListService {

    private final ShoppingListDao shoppingListDao;
    private final RecipeDao recipeDao;

    public ShoppingListService(ShoppingListDao shoppingListDao, RecipeDao recipeDao) {
        this.shoppingListDao = shoppingListDao;
        this.recipeDao = recipeDao;
    }

    public ShoppingListResponse get(User user) {
        return toResponse(shoppingListDao.findByUser(user.getId()));
    }

    public ShoppingListResponse addItem(User user, AddItemRequest request) {
        List<ShoppingItem> items = shoppingListDao.findByUser(user.getId());
        items.add(new ShoppingItem(UUID.randomUUID().toString(), user.getId(), request.name().trim(),
                request.quantity(), request.unit() == null ? "piece" : request.unit(),
                false, null, items.size()));
        shoppingListDao.replaceAll(user.getId(), items);
        return toResponse(items);
    }

    public ShoppingListResponse addRecipe(User user, AddRecipeRequest request) {
        Recipe recipe = recipeDao.findById(request.recipeId())
                .orElseThrow(() -> new NotFoundException("Recipe not found."));
        int servings = request.servings() == null ? recipe.getServings() : request.servings();
        double factor = (double) servings / Math.max(1, recipe.getServings());

        List<ShoppingItem> items = shoppingListDao.findByUser(user.getId());
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (ingredient.isOptional()) {
                continue; // optional lines stay off the shopping list
            }
            double quantity = Math.round(ingredient.getQuantity() * factor * 1000.0) / 1000.0;
            Optional<ShoppingItem> match = items.stream()
                    .filter(item -> item.getName().equalsIgnoreCase(ingredient.getName())
                            && item.getUnit().equalsIgnoreCase(ingredient.getUnit()))
                    .findFirst();
            if (match.isPresent()) {
                match.get().setQuantity(
                        Math.round((match.get().getQuantity() + quantity) * 1000.0) / 1000.0);
            } else {
                items.add(new ShoppingItem(UUID.randomUUID().toString(), user.getId(),
                        ingredient.getName(), quantity, ingredient.getUnit(), false,
                        recipe.getTitle(), items.size()));
            }
        }
        shoppingListDao.replaceAll(user.getId(), items);
        return toResponse(items);
    }

    public ShoppingListResponse toggle(User user, String itemId, ToggleItemRequest request) {
        List<ShoppingItem> items = shoppingListDao.findByUser(user.getId());
        items.stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setChecked(request.checked()));
        shoppingListDao.replaceAll(user.getId(), items);
        return toResponse(items);
    }

    public ShoppingListResponse remove(User user, String itemId) {
        List<ShoppingItem> items = new ArrayList<>(
                shoppingListDao.findByUser(user.getId()).stream()
                        .filter(item -> !item.getId().equals(itemId))
                        .toList());
        shoppingListDao.replaceAll(user.getId(), items);
        return toResponse(items);
    }

    public ShoppingListResponse clearChecked(User user) {
        List<ShoppingItem> items = new ArrayList<>(shoppingListDao.findByUser(user.getId()).stream()
                .filter(item -> !item.isChecked())
                .toList());
        shoppingListDao.replaceAll(user.getId(), items);
        return toResponse(items);
    }

    public ShoppingListResponse clearAll(User user) {
        shoppingListDao.replaceAll(user.getId(), List.of());
        return new ShoppingListResponse(List.of());
    }

    private ShoppingListResponse toResponse(List<ShoppingItem> items) {
        List<ShoppingItemResponse> mapped = items.stream()
                .map(item -> new ShoppingItemResponse(item.getId(), item.getName(), item.getQuantity(),
                        item.getUnit(), item.isChecked(), item.getRecipeTitle()))
                .toList();
        return new ShoppingListResponse(mapped);
    }
}

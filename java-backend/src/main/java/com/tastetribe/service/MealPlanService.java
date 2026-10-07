package com.tastetribe.service;

import com.tastetribe.dao.MealPlanDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.ShoppingListDao;
import com.tastetribe.dto.MealPlanDtos.AddMealRequest;
import com.tastetribe.dto.MealPlanDtos.MealPlanEntryResponse;
import com.tastetribe.dto.MealPlanDtos.MealPlanResponse;
import com.tastetribe.dto.MealPlanDtos.MoveMealRequest;
import com.tastetribe.dto.ShoppingDtos.ShoppingItemResponse;
import com.tastetribe.dto.ShoppingDtos.ShoppingListResponse;
import com.tastetribe.exception.BadRequestException;
import com.tastetribe.exception.ForbiddenException;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.MealPlanEntry;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.ShoppingItem;
import com.tastetribe.model.User;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Weekly meal planner business logic.
 *
 * <p>Weeks always start on Monday (normalised server-side so the client can never
 * disagree). "Send the week to my shopping list" aggregates every planned recipe's
 * ingredients, scales each one to its planned serving count and merges identical
 * name+unit lines — the same Java arithmetic the recipe scaler uses.</p>
 */
@Service
public class MealPlanService {

    private final MealPlanDao mealPlanDao;
    private final RecipeDao recipeDao;
    private final ShoppingListDao shoppingListDao;

    public MealPlanService(MealPlanDao mealPlanDao, RecipeDao recipeDao, ShoppingListDao shoppingListDao) {
        this.mealPlanDao = mealPlanDao;
        this.recipeDao = recipeDao;
        this.shoppingListDao = shoppingListDao;
    }

    /** Monday of the week containing {@code date} (or today when null/blank). */
    public static LocalDate weekStart(String date) {
        LocalDate anchor = parseDate(date, LocalDate.now());
        return anchor.with(DayOfWeek.MONDAY);
    }

    private static LocalDate parseDate(String value, LocalDate fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Dates must look like YYYY-MM-DD.");
        }
    }

    public MealPlanResponse week(User user, String weekOf) {
        LocalDate start = weekStart(weekOf);
        LocalDate end = start.plusDays(6);
        List<MealPlanEntryResponse> entries = mealPlanDao.findInRange(user.getId(), start, end).stream()
                .map(MealPlanService::toResponse)
                .toList();
        return new MealPlanResponse(start.toString(), entries);
    }

    public MealPlanResponse add(User user, AddMealRequest request) {
        Recipe recipe = recipeDao.findById(request.recipeId())
                .orElseThrow(() -> new NotFoundException("Recipe not found."));
        MealPlanEntry entry = new MealPlanEntry();
        entry.setUserId(user.getId());
        entry.setRecipeId(recipe.getId());
        entry.setPlanDate(parseDate(request.planDate(), LocalDate.now()));
        entry.setSlot(request.slot().toLowerCase());
        entry.setServings(request.servings() == null ? recipe.getServings() : request.servings());
        mealPlanDao.save(entry);
        return week(user, entry.getPlanDate().toString());
    }

    public MealPlanResponse move(User user, String entryId, MoveMealRequest request) {
        MealPlanEntry entry = requireOwned(user, entryId);
        LocalDate target = parseDate(request.planDate(), entry.getPlanDate());
        mealPlanDao.move(entry.getId(), target, request.slot().toLowerCase());
        return week(user, target.toString());
    }

    public MealPlanResponse remove(User user, String entryId) {
        MealPlanEntry entry = requireOwned(user, entryId);
        mealPlanDao.deleteById(entry.getId());
        return week(user, entry.getPlanDate().toString());
    }

    /**
     * Merge every planned meal of the week into the user's shopping list.
     *
     * @return the resulting shopping list
     */
    public ShoppingListResponse sendWeekToShoppingList(User user, String weekOf) {
        LocalDate start = weekStart(weekOf);
        List<MealPlanEntry> planned = mealPlanDao.findInRange(user.getId(), start, start.plusDays(6));
        if (planned.isEmpty()) {
            throw new BadRequestException("There are no meals planned for that week yet.");
        }

        Map<String, Recipe> recipes = recipeDao.findByIds(
                        planned.stream().map(MealPlanEntry::getRecipeId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));

        List<ShoppingItem> items = shoppingListDao.findByUser(user.getId());
        for (MealPlanEntry entry : planned) {
            Recipe recipe = recipes.get(entry.getRecipeId());
            if (recipe == null) {
                continue; // recipe was deleted after planning
            }
            double factor = (double) entry.getServings() / Math.max(1, recipe.getServings());
            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                if (ingredient.isOptional()) {
                    continue;
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
                            "Meal plan", items.size()));
                }
            }
        }
        shoppingListDao.replaceAll(user.getId(), items);
        return new ShoppingListResponse(items.stream()
                .map(item -> new ShoppingItemResponse(item.getId(), item.getName(), item.getQuantity(),
                        item.getUnit(), item.isChecked(), item.getRecipeTitle()))
                .toList());
    }

    private MealPlanEntry requireOwned(User user, String entryId) {
        MealPlanEntry entry = mealPlanDao.findById(entryId)
                .orElseThrow(() -> new NotFoundException("That planned meal no longer exists."));
        if (!entry.getUserId().equals(user.getId())) {
            throw new ForbiddenException("That planned meal belongs to someone else.");
        }
        return entry;
    }

    private static MealPlanEntryResponse toResponse(MealPlanEntry entry) {
        return new MealPlanEntryResponse(
                entry.getId(),
                entry.getRecipeId(),
                entry.getRecipeTitle(),
                entry.getRecipeImage(),
                entry.getRecipeTotalTime(),
                entry.getPlanDate().toString(),
                entry.getSlot(),
                entry.getServings());
    }
}

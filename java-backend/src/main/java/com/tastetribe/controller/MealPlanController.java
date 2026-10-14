package com.tastetribe.controller;

import com.tastetribe.dto.MealPlanDtos.AddMealRequest;
import com.tastetribe.dto.MealPlanDtos.MealPlanResponse;
import com.tastetribe.dto.MealPlanDtos.MoveMealRequest;
import com.tastetribe.dto.ShoppingDtos.ShoppingListResponse;
import com.tastetribe.service.MealPlanService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Weekly meal planner — {@code /api/meal-plan}. Always scoped to the logged-in user. */
@RestController
@RequestMapping("/meal-plan")
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final SessionContext session;

    public MealPlanController(MealPlanService mealPlanService, SessionContext session) {
        this.mealPlanService = mealPlanService;
        this.session = session;
    }

    /** @param weekOf any date inside the desired week (YYYY-MM-DD); defaults to this week. */
    @GetMapping
    public MealPlanResponse week(@RequestParam(required = false) String weekOf,
                                 HttpServletRequest request) {
        return mealPlanService.week(session.require(request), weekOf);
    }

    @PostMapping("/entries")
    public MealPlanResponse add(@Valid @RequestBody AddMealRequest body, HttpServletRequest request) {
        return mealPlanService.add(session.require(request), body);
    }

    /** Drag-and-drop: move a planned meal to a different day/slot. */
    @PatchMapping("/entries/{entryId}")
    public MealPlanResponse move(@PathVariable String entryId,
                                 @Valid @RequestBody MoveMealRequest body,
                                 HttpServletRequest request) {
        return mealPlanService.move(session.require(request), entryId, body);
    }

    @DeleteMapping("/entries/{entryId}")
    public MealPlanResponse remove(@PathVariable String entryId, HttpServletRequest request) {
        return mealPlanService.remove(session.require(request), entryId);
    }

    /** Merge every planned meal of the week into the shopping list. */
    @PostMapping("/shopping-list")
    public ShoppingListResponse toShoppingList(@RequestParam(required = false) String weekOf,
                                               HttpServletRequest request) {
        return mealPlanService.sendWeekToShoppingList(session.require(request), weekOf);
    }
}

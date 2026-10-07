package com.tastetribe.controller;

import com.tastetribe.dto.ShoppingDtos.AddItemRequest;
import com.tastetribe.dto.ShoppingDtos.AddRecipeRequest;
import com.tastetribe.dto.ShoppingDtos.ShoppingListResponse;
import com.tastetribe.dto.ShoppingDtos.ToggleItemRequest;
import com.tastetribe.service.ShoppingListService;
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
import org.springframework.web.bind.annotation.RestController;

/** Persistent shopping list — {@code /api/shopping-list}. Always user-scoped. */
@RestController
@RequestMapping("/shopping-list")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;
    private final SessionContext session;

    public ShoppingListController(ShoppingListService shoppingListService, SessionContext session) {
        this.shoppingListService = shoppingListService;
        this.session = session;
    }

    @GetMapping
    public ShoppingListResponse get(HttpServletRequest request) {
        return shoppingListService.get(session.require(request));
    }

    @PostMapping("/items")
    public ShoppingListResponse addItem(@Valid @RequestBody AddItemRequest body, HttpServletRequest request) {
        return shoppingListService.addItem(session.require(request), body);
    }

    /** Merge a recipe's ingredients (optionally re-scaled) into the list. */
    @PostMapping("/add-recipe")
    public ShoppingListResponse addRecipe(@Valid @RequestBody AddRecipeRequest body, HttpServletRequest request) {
        return shoppingListService.addRecipe(session.require(request), body);
    }

    @PatchMapping("/items/{itemId}")
    public ShoppingListResponse toggle(@PathVariable String itemId,
                                       @Valid @RequestBody ToggleItemRequest body,
                                       HttpServletRequest request) {
        return shoppingListService.toggle(session.require(request), itemId, body);
    }

    @DeleteMapping("/items/{itemId}")
    public ShoppingListResponse remove(@PathVariable String itemId, HttpServletRequest request) {
        return shoppingListService.remove(session.require(request), itemId);
    }

    @PostMapping("/clear-checked")
    public ShoppingListResponse clearChecked(HttpServletRequest request) {
        return shoppingListService.clearChecked(session.require(request));
    }

    @DeleteMapping
    public ShoppingListResponse clearAll(HttpServletRequest request) {
        return shoppingListService.clearAll(session.require(request));
    }
}

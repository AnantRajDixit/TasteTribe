package com.tastetribe.controller;

import com.tastetribe.dto.RecipeDtos.CategoryRequest;
import com.tastetribe.dto.RecipeDtos.CategoryResponse;
import com.tastetribe.service.CategoryService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Category listing (public) and management (admin only) — {@code /api/categories}. */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SessionContext session;

    public CategoryController(CategoryService categoryService, SessionContext session) {
        this.categoryService = categoryService;
        this.session = session;
    }

    @GetMapping
    public List<CategoryResponse> list() {
        return categoryService.list();
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest body,
                                                   HttpServletRequest request) {
        session.requireAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(body));
    }

    @PatchMapping("/{id}")
    public CategoryResponse update(@PathVariable String id, @Valid @RequestBody CategoryRequest body,
                                   HttpServletRequest request) {
        session.requireAdmin(request);
        return categoryService.update(id, body);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable String id, HttpServletRequest request) {
        session.requireAdmin(request);
        categoryService.delete(id);
        return Map.of("ok", true);
    }
}

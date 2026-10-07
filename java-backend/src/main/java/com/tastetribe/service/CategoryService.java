package com.tastetribe.service;

import com.tastetribe.dao.CategoryDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dto.RecipeDtos.CategoryRequest;
import com.tastetribe.dto.RecipeDtos.CategoryResponse;
import com.tastetribe.exception.DuplicateResourceException;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Category;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Category management (admin mutations, public listings with live recipe counts). */
@Service
public class CategoryService {

    private final CategoryDao categoryDao;
    private final RecipeDao recipeDao;

    public CategoryService(CategoryDao categoryDao, RecipeDao recipeDao) {
        this.categoryDao = categoryDao;
        this.recipeDao = recipeDao;
    }

    public List<CategoryResponse> list() {
        Map<String, Long> counts = new HashMap<>(recipeDao.countByCategory());
        return categoryDao.findAll().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getName(),
                        category.getSlug(),
                        category.getDescription(),
                        category.getImageUrl(),
                        counts.getOrDefault(category.getName(), 0L)))
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        String slug = slugify(request.name());
        if (categoryDao.existsBySlug(slug)) {
            throw new DuplicateResourceException("A category with that name already exists.");
        }
        Category category = new Category();
        category.setName(request.name().trim());
        category.setSlug(slug);
        category.setDescription(request.description() == null ? "" : request.description());
        category.setImageUrl(request.imageUrl());
        categoryDao.save(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                category.getDescription(), category.getImageUrl(), 0);
    }

    public CategoryResponse update(String id, CategoryRequest request) {
        Category category = categoryDao.findById(id).orElseThrow(() -> new NotFoundException("Category not found."));
        category.setName(request.name().trim());
        category.setSlug(slugify(request.name()));
        category.setDescription(request.description() == null ? "" : request.description());
        category.setImageUrl(request.imageUrl());
        categoryDao.update(category);
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                category.getDescription(), category.getImageUrl(), 0);
    }

    public void delete(String id) {
        if (categoryDao.findById(id).isEmpty()) {
            throw new NotFoundException("Category not found.");
        }
        categoryDao.deleteById(id);
    }

    private static String slugify(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}

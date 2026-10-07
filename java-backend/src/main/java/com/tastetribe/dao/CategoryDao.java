package com.tastetribe.dao;

import com.tastetribe.model.Category;
import java.util.Optional;

/** Data-access contract for {@link Category} rows. */
public interface CategoryDao extends GenericDao<Category, String> {

    Optional<Category> findBySlug(String slug);

    boolean existsBySlug(String slug);

    void update(Category category);
}

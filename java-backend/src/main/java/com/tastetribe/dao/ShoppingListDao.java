package com.tastetribe.dao;

import com.tastetribe.model.ShoppingItem;
import java.util.List;

/** Data-access contract for the per-user shopping list (replace-all semantics). */
public interface ShoppingListDao {

    List<ShoppingItem> findByUser(String userId);

    void replaceAll(String userId, List<ShoppingItem> items);
}

package com.tastetribe.dao;

import com.tastetribe.model.MealPlanEntry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Data-access contract for the weekly meal planner. */
public interface MealPlanDao {

    /** All entries for a user within an inclusive date range, with recipe display fields joined. */
    List<MealPlanEntry> findInRange(String userId, LocalDate from, LocalDate to);

    Optional<MealPlanEntry> findById(String id);

    void save(MealPlanEntry entry);

    /** Move an existing entry to another date/slot (drag and drop). */
    void move(String id, LocalDate planDate, String slot);

    void deleteById(String id);

    void deleteAllForUser(String userId);
}

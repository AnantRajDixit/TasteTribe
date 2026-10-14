package com.tastetribe.service;

import com.tastetribe.dao.RecentlyViewedDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.model.User;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.stereotype.Service;

/**
 * Multithreaded view counter (Multithreading & Synchronization rubric area).
 *
 * <p>Recipe views arrive on every request — writing to MySQL on each hit would be
 * wasteful. Instead, increments land in lock-free {@link LongAdder} cells inside a
 * {@link ConcurrentHashMap}, and a single daemon {@link ScheduledExecutorService}
 * thread drains them every 5 seconds into batched UPDATE statements. The drain is
 * {@code synchronized} so a shutdown-flush and a scheduled flush can never interleave.</p>
 */
@Service
public class ViewTrackerService {

    private final RecipeDao recipeDao;
    private final RecentlyViewedDao recentlyViewedDao;

    /** recipeId -> pending view delta (lock-free counters). */
    private final ConcurrentHashMap<String, LongAdder> pendingViews = new ConcurrentHashMap<>();

    /** "userId|recipeId" markers waiting to become recently_viewed rows. */
    private final ConcurrentHashMap<String, Boolean> pendingUserViews = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(task -> {
        Thread thread = new Thread(task, "view-tracker-flusher");
        thread.setDaemon(true);
        return thread;
    });

    public ViewTrackerService(RecipeDao recipeDao, RecentlyViewedDao recentlyViewedDao) {
        this.recipeDao = recipeDao;
        this.recentlyViewedDao = recentlyViewedDao;
    }

    @PostConstruct
    public void start() {
        scheduler.scheduleWithFixedDelay(this::flush, 5, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void stop() {
        flush();
        scheduler.shutdownNow();
    }

    /** Called on every published-recipe page view — never blocks on the database. */
    public void record(String recipeId, User user) {
        pendingViews.computeIfAbsent(recipeId, key -> new LongAdder()).increment();
        if (user != null) {
            pendingUserViews.putIfAbsent(user.getId() + "|" + recipeId, Boolean.TRUE);
        }
    }

    private synchronized void flush() {
        // Drain view counters.
        Map<String, Integer> deltas = new HashMap<>();
        pendingViews.forEach((recipeId, adder) -> {
            long value = adder.sumThenReset();
            if (value > 0) {
                deltas.merge(recipeId, (int) value, Integer::sum);
            }
        });
        if (!deltas.isEmpty()) {
            recipeDao.applyViewDeltas(deltas);
        }
        // Drain recently-viewed markers.
        if (!pendingUserViews.isEmpty()) {
            pendingUserViews.keySet().forEach(key -> {
                String[] parts = key.split("\\|", 2);
                if (parts.length == 2) {
                    recentlyViewedDao.upsert(parts[0], parts[1]);
                }
            });
            pendingUserViews.clear();
        }
    }
}

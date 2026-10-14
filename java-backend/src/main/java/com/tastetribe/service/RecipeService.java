package com.tastetribe.service;

import com.tastetribe.dao.CommentDao;
import com.tastetribe.dao.FavoriteDao;
import com.tastetribe.dao.FollowDao;
import com.tastetribe.dao.LikeDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.RecipeQuery;
import com.tastetribe.dao.ReviewDao;
import com.tastetribe.dao.impl.RecipeDaoImpl;
import com.tastetribe.dto.RecipeDtos.IngredientRequest;
import com.tastetribe.dto.RecipeDtos.NutritionRequest;
import com.tastetribe.dto.RecipeDtos.RecipePage;
import com.tastetribe.dto.RecipeDtos.RecipeRequest;
import com.tastetribe.dto.RecipeDtos.RecipeResponse;
import com.tastetribe.dto.RecipeDtos.ScaleResponse;
import com.tastetribe.dto.RecipeDtos.ScaledIngredientResponse;
import com.tastetribe.dto.SocialDtos.ToggleResponse;
import com.tastetribe.exception.ForbiddenException;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.exception.UnauthorizedException;
import com.tastetribe.model.Difficulty;
import com.tastetribe.model.Nutrition;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeIngredient;
import com.tastetribe.model.RecipeStatus;
import com.tastetribe.model.Role;
import com.tastetribe.model.User;
import com.tastetribe.util.QuantityFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Recipe business logic: CRUD with ownership authorization, discovery search,
 * the serving scaler (pure backend calculation), like/favorite toggles and the
 * personalized feed tabs.
 */
@Service
public class RecipeService {

    private final RecipeDao recipeDao;
    private final LikeDao likeDao;
    private final FavoriteDao favoriteDao;
    private final ReviewDao reviewDao;
    private final CommentDao commentDao;
    private final FollowDao followDao;
    private final ViewTrackerService viewTracker;
    private final QuantityFormatter quantityFormatter;

    public RecipeService(RecipeDao recipeDao, LikeDao likeDao, FavoriteDao favoriteDao,
                         ReviewDao reviewDao, CommentDao commentDao, FollowDao followDao,
                         ViewTrackerService viewTracker, QuantityFormatter quantityFormatter) {
        this.recipeDao = recipeDao;
        this.likeDao = likeDao;
        this.favoriteDao = favoriteDao;
        this.reviewDao = reviewDao;
        this.commentDao = commentDao;
        this.followDao = followDao;
        this.viewTracker = viewTracker;
        this.quantityFormatter = quantityFormatter;
    }

    // ---------- queries ----------

    public RecipePage search(RecipeQuery query, String sort, int page, int limit, User user) {
        String orderBy = RecipeDaoImpl.orderByFor(sort);
        long total = recipeDao.countSearch(query);
        List<Recipe> rows = recipeDao.search(query, orderBy, (page - 1) * limit, limit);
        return toPage(rows, total, page, limit, user);
    }

    public RecipeResponse get(String id, User user) {
        Recipe recipe = recipeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Recipe not found."));
        if (recipe.getStatus() == RecipeStatus.DRAFT && !canTouch(recipe, user)) {
            throw new NotFoundException("Recipe not found.");
        }
        if (recipe.getStatus() == RecipeStatus.PUBLISHED) {
            viewTracker.record(id, user);
        }
        Marked marked = markedIds(user);
        return RecipeResponse.from(recipe, marked.liked().contains(id), marked.favs().contains(id));
    }

    public RecipePage feed(String tab, int page, int limit, User user) {
        Set<String> empty = Set.of();
        switch (tab) {
            case "following" -> {
                requireLogin(user);
                List<String> ids = followDao.followingIds(user.getId());
                RecipeQuery query = new RecipeQuery(null, null, null, null, null, null, null, null,
                        RecipeStatus.PUBLISHED);
                if (ids.isEmpty()) {
                    return new RecipePage(List.of(), 0, page, 1);
                }
                // Author filter applied post-query (kept simple for the demo scale).
                List<Recipe> rows = recipeDao.search(query, "created_at DESC", 0, 400).stream()
                        .filter(r -> ids.contains(r.getAuthorId()))
                        .toList();
                long total = rows.size();
                List<Recipe> slice = slice(rows, page, limit);
                return toPage(slice, total, page, limit, user);
            }
            case "recommended" -> {
                requireLogin(user);
                Set<String> interacted = new HashSet<>();
                interacted.addAll(likeDao.recipeIdsForUser(user.getId()));
                interacted.addAll(favoriteDao.recipeIdsForUser(user.getId()));
                interacted.addAll(reviewDao.recipeIdsForUser(user.getId()));
                List<Recipe> seeds = interacted.isEmpty() ? List.of() : recipeDao.findByIds(interacted);
                Set<String> categories = seeds.stream().map(Recipe::getCategory).collect(Collectors.toSet());
                Set<String> cuisines = seeds.stream().map(Recipe::getCuisine).collect(Collectors.toSet());
                List<Recipe> candidates = recipeDao.findPublished(400).stream()
                        .filter(r -> !r.getAuthorId().equals(user.getId()))
                        .filter(r -> !interacted.contains(r.getId()))
                        .filter(r -> seeds.isEmpty()
                                || categories.contains(r.getCategory())
                                || cuisines.contains(r.getCuisine()))
                        .sorted((a, b) -> Double.compare(
                                b.getAvgRating() == null ? 0 : b.getAvgRating(),
                                a.getAvgRating() == null ? 0 : a.getAvgRating()))
                        .toList();
                long total = candidates.size();
                return toPage(slice(candidates, page, limit), total, page, limit, user);
            }
            case "trending" -> {
                // Popularity score computed in Java over the published set.
                List<Recipe> rows = recipeDao.findPublished(400).stream()
                        .sorted((a, b) -> Double.compare(score(b), score(a)))
                        .toList();
                long total = rows.size();
                return toPage(slice(rows, page, limit), total, page, limit, user);
            }
            default -> {
                // "latest"
                List<Recipe> rows = recipeDao.findPublished(400);
                long total = rows.size();
                return toPage(slice(rows, page, limit), total, page, limit, user);
            }
        }
    }

    private static double score(Recipe recipe) {
        return recipe.getLikesCount() * 2.0
                + recipe.getFavoritesCount() * 2.0
                + recipe.getRatingsCount() * 3.0
                + recipe.getViews() / 10.0
                + (recipe.getAvgRating() == null ? 0 : recipe.getAvgRating());
    }

    private static <T> List<T> slice(List<T> list, int page, int limit) {
        int from = Math.min((page - 1) * limit, list.size());
        int to = Math.min(from + limit, list.size());
        return list.subList(from, to);
    }

    // ---------- mutations ----------

    public RecipeResponse create(User user, RecipeRequest request) {
        Recipe recipe = fromRequest(new Recipe(), request);
        recipe.setAuthorId(user.getId());
        recipe.setAuthorName(user.getName());
        recipe.setAuthorUsername(user.getUsername());
        recipe.setStatus(RecipeStatus.from(request.status()));
        recipe.setSource("user");
        recipe.setViews(0);
        recipe.setLikesCount(0);
        recipe.setFavoritesCount(0);
        recipe.setRatingsCount(0);
        recipeDao.save(recipe);
        Marked marked = markedIds(user);
        return RecipeResponse.from(recipe, marked.liked().contains(recipe.getId()),
                marked.favs().contains(recipe.getId()));
    }

    public RecipeResponse update(String id, User user, RecipeRequest request) {
        Recipe recipe = requireRecipe(id);
        if (!canTouch(recipe, user)) {
            throw new ForbiddenException("Only the recipe owner can edit this recipe.");
        }
        fromRequest(recipe, request);
        recipeDao.update(recipe);
        Marked marked = markedIds(user);
        return RecipeResponse.from(recipe, marked.liked().contains(id), marked.favs().contains(id));
    }

    public void delete(String id, User user) {
        Recipe recipe = requireRecipe(id);
        if (!canTouch(recipe, user)) {
            throw new ForbiddenException("Only the recipe owner or an admin can delete this recipe.");
        }
        recipeDao.deleteById(id);
        reviewDao.deleteAllForRecipe(id);
        commentDao.deleteForRecipe(id);
        likeDao.deleteAllForRecipe(id);
        favoriteDao.deleteAllForRecipe(id);
    }

    // ---------- like / favorite toggles ----------

    public ToggleResponse toggleLike(String id, User user) {
        requireRecipe(id);
        boolean active = !likeDao.exists(user.getId(), id);
        likeDao.set(user.getId(), id, active);
        long count = likeDao.countByRecipe(id);
        recipeDao.updateCounter(id, "likes_count", (int) count);
        return new ToggleResponse(active, count);
    }

    public ToggleResponse toggleFavorite(String id, User user) {
        requireRecipe(id);
        boolean active = !favoriteDao.exists(user.getId(), id);
        favoriteDao.set(user.getId(), id, active);
        long count = favoriteDao.countByRecipe(id);
        recipeDao.updateCounter(id, "favorites_count", (int) count);
        return new ToggleResponse(active, count);
    }

    // ---------- serving scaler (backend business logic) ----------

    public ScaleResponse scale(String id, int servings) {
        Recipe recipe = requireRecipe(id);
        int base = Math.max(1, recipe.getServings());
        double factor = (double) servings / base;
        List<ScaledIngredientResponse> lines = recipe.getIngredients().stream()
                .map(ing -> {
                    double quantity = Math.round(ing.getQuantity() * factor * 1000.0) / 1000.0;
                    String display = (quantityFormatter.format(quantity) + " " + ing.getUnit() + " "
                            + ing.getName()).trim();
                    return new ScaledIngredientResponse(ing.getName(), quantity, ing.getUnit(),
                            ing.isOptional(), display);
                })
                .toList();
        return new ScaleResponse(id, base, servings, lines);
    }

    // ---------- helpers ----------

    private record Marked(Set<String> liked, Set<String> favs) {
    }

    private Marked markedIds(User user) {
        if (user == null) {
            return new Marked(Set.of(), Set.of());
        }
        return new Marked(likeDao.recipeIdsForUser(user.getId()), favoriteDao.recipeIdsForUser(user.getId()));
    }

    public boolean canTouch(Recipe recipe, User user) {
        return user != null
                && (user.getId().equals(recipe.getAuthorId()) || user.getRole() == Role.ADMIN);
    }

    private Recipe requireRecipe(String id) {
        return recipeDao.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found."));
    }

    private static void requireLogin(User user) {
        if (user == null) {
            throw new UnauthorizedException("Log in to use this part of the feed.");
        }
    }

    /** Maps request DTO onto the entity (total time always derived). */
    private Recipe fromRequest(Recipe recipe, RecipeRequest request) {
        recipe.setTitle(request.title().trim());
        recipe.setDescription(request.description() == null ? "" : request.description());
        recipe.setCoverImage(request.coverImage());
        recipe.setCuisine(request.cuisine());
        recipe.setCategory(request.category());
        recipe.setDifficulty(Difficulty.from(request.difficulty()));
        recipe.setDietary(request.dietary() == null || request.dietary().isBlank()
                ? "Non-Vegetarian" : request.dietary());
        recipe.setPrepTime(request.prepTime());
        recipe.setCookTime(request.cookTime());
        recipe.setTotalTime(request.prepTime() + request.cookTime());
        recipe.setServings(request.servings());
        recipe.setIngredients(request.ingredients().stream()
                .map(IngredientRequest -> {
                    RecipeIngredient ing = new RecipeIngredient(
                            IngredientRequest.name().trim(),
                            IngredientRequest.quantity(),
                            IngredientRequest.unit() == null ? "piece" : IngredientRequest.unit(),
                            IngredientRequest.optional());
                    return ing;
                })
                .toList());
        recipe.setInstructions(request.instructions());
        recipe.setTags(request.tags() == null ? List.of() : request.tags());
        recipe.setNutrition(request.nutrition() == null ? null
                : new Nutrition(request.nutrition().calories(), request.nutrition().protein(),
                request.nutrition().carbs(), request.nutrition().fat()));
        return recipe;
    }

    private RecipePage toPage(List<Recipe> rows, long total, int page, int limit, User user) {
        Marked marked = markedIds(user);
        List<RecipeResponse> items = rows.stream()
                .map(recipe -> RecipeResponse.from(recipe,
                        marked.liked().contains(recipe.getId()),
                        marked.favs().contains(recipe.getId())))
                .toList();
        int pages = (int) Math.max(1, Math.ceil((double) total / limit));
        return new RecipePage(items, total, page, pages);
    }
}

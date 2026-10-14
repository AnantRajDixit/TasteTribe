package com.tastetribe.service;

import com.tastetribe.dao.FavoriteDao;
import com.tastetribe.dao.FollowDao;
import com.tastetribe.dao.LikeDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.RecipeQuery;
import com.tastetribe.dao.ReviewDao;
import com.tastetribe.dao.UserDao;
import com.tastetribe.dto.AuthDtos.FollowResponse;
import com.tastetribe.dto.AuthDtos.ProfileResponse;
import com.tastetribe.dto.AuthDtos.UserResponse;
import com.tastetribe.dto.RecipeDtos.RecipeResponse;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.RecipeStatus;
import com.tastetribe.model.Role;
import com.tastetribe.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Profile business logic: public profile with community counters (uploaded recipes,
 * followers, following, average recipe rating), follow/unfollow and per-user recipes.
 */
@Service
public class ProfileService {

    private final UserDao userDao;
    private final RecipeDao recipeDao;
    private final FollowDao followDao;
    private final LikeDao likeDao;
    private final FavoriteDao favoriteDao;

    public ProfileService(UserDao userDao, RecipeDao recipeDao, FollowDao followDao,
                          LikeDao likeDao, FavoriteDao favoriteDao) {
        this.userDao = userDao;
        this.recipeDao = recipeDao;
        this.followDao = followDao;
        this.likeDao = likeDao;
        this.favoriteDao = favoriteDao;
    }

    public ProfileResponse profile(String username, User viewer) {
        User profile = userDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Chef not found."));
        List<Recipe> published = recipeDao.findByAuthor(profile.getId(), RecipeStatus.PUBLISHED);

        // Average of the author's own recipe ratings (Java streams over the aggregate column).
        List<Double> ratings = published.stream()
                .map(Recipe::getAvgRating)
                .filter(java.util.Objects::nonNull)
                .toList();
        Double avgRating = ratings.isEmpty() ? null
                : Math.round(ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100.0) / 100.0;

        boolean isSelf = viewer != null && viewer.getId().equals(profile.getId());
        boolean isAdmin = viewer != null && viewer.getRole() == Role.ADMIN;
        boolean isFollowing = viewer != null
                && followDao.isFollowing(viewer.getId(), profile.getId());

        return new ProfileResponse(
                profile.getId(),
                profile.getName(),
                profile.getUsername(),
                (isSelf || isAdmin) ? profile.getEmail() : null,
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getRole().json(),
                profile.getCreatedAt() == null ? null : profile.getCreatedAt().toInstant(java.time.ZoneOffset.UTC),
                followDao.countFollowers(profile.getId()),
                followDao.countFollowing(profile.getId()),
                published.size(),
                avgRating,
                isFollowing);
    }

    public List<RecipeResponse> recipes(String username, String status, User viewer) {
        User profile = userDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Chef not found."));
        boolean isSelf = viewer != null
                && (viewer.getId().equals(profile.getId()) || viewer.getRole() == Role.ADMIN);
        RecipeStatus statusFilter = isSelf && "draft".equalsIgnoreCase(status) ? RecipeStatus.DRAFT : null;
        boolean draftsOnly = statusFilter == RecipeStatus.DRAFT;
        List<Recipe> rows = isSelf && (status == null || "all".equalsIgnoreCase(status) || draftsOnly)
                ? recipeDao.findByAuthor(profile.getId(), draftsOnly ? RecipeStatus.DRAFT : null)
                : recipeDao.findByAuthor(profile.getId(), RecipeStatus.PUBLISHED);

        Set<String> liked = viewer == null ? Set.of() : likeDao.recipeIdsForUser(viewer.getId());
        Set<String> favs = viewer == null ? Set.of() : favoriteDao.recipeIdsForUser(viewer.getId());
        return rows.stream()
                .map(recipe -> RecipeResponse.from(recipe, liked.contains(recipe.getId()),
                        favs.contains(recipe.getId())))
                .toList();
    }

    public List<UserResponse> followers(String username) {
        User profile = requireUser(username);
        return userDao.findAllByIds(followDao.followerIds(profile.getId())).values().stream()
                .map(user -> UserResponse.from(user, false))
                .toList();
    }

    public List<UserResponse> following(String username) {
        User profile = requireUser(username);
        return userDao.findAllByIds(followDao.followingIds(profile.getId())).values().stream()
                .map(user -> UserResponse.from(user, false))
                .toList();
    }

    public FollowResponse follow(User viewer, String username) {
        User target = requireUser(username);
        if (target.getId().equals(viewer.getId())) {
            throw new com.tastetribe.exception.BadRequestException("You can't follow yourself.");
        }
        followDao.follow(viewer.getId(), target.getId());
        return new FollowResponse(true, followDao.countFollowers(target.getId()));
    }

    public FollowResponse unfollow(User viewer, String username) {
        User target = requireUser(username);
        followDao.unfollow(viewer.getId(), target.getId());
        return new FollowResponse(false, followDao.countFollowers(target.getId()));
    }

    private User requireUser(String username) {
        Optional<User> found = userDao.findByUsername(username);
        if (found.isEmpty()) {
            throw new NotFoundException("Chef not found.");
        }
        return found.get();
    }
}

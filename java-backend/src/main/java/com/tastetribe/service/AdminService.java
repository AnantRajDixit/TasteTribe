package com.tastetribe.service;

import com.tastetribe.dao.CommentDao;
import com.tastetribe.dao.FavoriteDao;
import com.tastetribe.dao.FollowDao;
import com.tastetribe.dao.LikeDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.ReportDao;
import com.tastetribe.dao.ReviewDao;
import com.tastetribe.dao.SessionDao;
import com.tastetribe.dao.UserDao;
import com.tastetribe.dto.AdminDtos.AdminCommentRow;
import com.tastetribe.dto.AdminDtos.AdminRecipeRow;
import com.tastetribe.dto.AdminDtos.AdminReportRow;
import com.tastetribe.dto.AdminDtos.AdminUserRow;
import com.tastetribe.dto.AdminDtos.StatsResponse;
import com.tastetribe.dto.RecipeDtos.RecipeResponse;
import com.tastetribe.dto.SocialDtos.CommentResponse;
import com.tastetribe.exception.BadRequestException;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Comment;
import com.tastetribe.model.Recipe;
import com.tastetribe.model.Report;
import com.tastetribe.model.User;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Admin dashboard logic: platform statistics, moderation and account management. */
@Service
public class AdminService {

    private final UserDao userDao;
    private final RecipeDao recipeDao;
    private final ReviewDao reviewDao;
    private final CommentDao commentDao;
    private final ReportDao reportDao;
    private final SessionDao sessionDao;
    private final LikeDao likeDao;
    private final FavoriteDao favoriteDao;
    private final FollowDao followDao;
    private final RecipeService recipeService;

    public AdminService(UserDao userDao, RecipeDao recipeDao, ReviewDao reviewDao, CommentDao commentDao,
                        ReportDao reportDao, SessionDao sessionDao, LikeDao likeDao, FavoriteDao favoriteDao,
                        FollowDao followDao, RecipeService recipeService) {
        this.userDao = userDao;
        this.recipeDao = recipeDao;
        this.reviewDao = reviewDao;
        this.commentDao = commentDao;
        this.reportDao = reportDao;
        this.sessionDao = sessionDao;
        this.likeDao = likeDao;
        this.favoriteDao = favoriteDao;
        this.followDao = followDao;
        this.recipeService = recipeService;
    }

    public StatsResponse stats() {
        List<AdminUserRow> recentUsers = userDao.findRecent(6).stream()
                .map(user -> new AdminUserRow(user.getId(), user.getName(), user.getUsername(),
                        user.getEmail(), user.getRole().json(), 0,
                        user.getCreatedAt() == null ? null
                                : user.getCreatedAt().toInstant(java.time.ZoneOffset.UTC)))
                .toList();
        List<AdminRecipeRow> topRecipes = recipeDao.findPublished(100).stream()
                .sorted(Comparator.comparingInt(Recipe::getViews).reversed())
                .limit(5)
                .map(AdminService::toRecipeRow)
                .toList();
        return new StatsResponse(
                userDao.countAll(),
                recipeDao.countAll(),
                recipeDao.countByStatus(com.tastetribe.model.RecipeStatus.PUBLISHED),
                reviewDao.countAll(),
                commentDao.countAll(),
                reportDao.findPending().size(),
                recentUsers,
                topRecipes);
    }

    public List<AdminUserRow> users(String query) {
        List<User> rows = (query == null || query.isBlank())
                ? userDao.findAll()
                : userDao.search(query.trim(), 300);
        return rows.stream()
                .map(user -> new AdminUserRow(user.getId(), user.getName(), user.getUsername(),
                        user.getEmail(), user.getRole().json(),
                        countAuthorRecipes(user.getId()),
                        user.getCreatedAt() == null ? null
                                : user.getCreatedAt().toInstant(java.time.ZoneOffset.UTC)))
                .toList();
    }

    private long countAuthorRecipes(String authorId) {
        return recipeDao.findByAuthor(authorId, null).size();
    }

    public void deleteUser(User acting, String userId) {
        if (acting.getId().equals(userId)) {
            throw new BadRequestException("You cannot delete your own account here.");
        }
        User target = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));

        // Cascade: recipes (+ their social rows), then the user's own social rows.
        for (Recipe recipe : recipeDao.findByAuthor(userId, null)) {
            recipeDao.deleteById(recipe.getId());
            reviewDao.deleteAllForRecipe(recipe.getId());
            commentDao.deleteForRecipe(recipe.getId());
            likeDao.deleteAllForRecipe(recipe.getId());
            favoriteDao.deleteAllForRecipe(recipe.getId());
        }
        reviewDao.deleteAllForUser(userId);
        commentDao.deleteForUser(userId);
        likeDao.deleteAllForUser(userId);
        favoriteDao.deleteAllForUser(userId);
        followDao.deleteAllForUser(userId);
        sessionDao.deleteSessionsForUser(userId);
        userDao.delete(target.getId());
    }

    public List<AdminRecipeRow> recipes(String query, String status) {
        return recipeDao.findAll().stream()
                .filter(recipe -> status == null || status.isBlank()
                        || recipe.getStatus().name().equalsIgnoreCase(status))
                .filter(recipe -> query == null || query.isBlank()
                        || recipe.getTitle().toLowerCase().contains(query.toLowerCase()))
                .map(AdminService::toRecipeRow)
                .toList();
    }

    public List<CommentResponse> recentComments() {
        List<Comment> rows = commentDao.findRecent(60);
        Map<String, Recipe> recipes = recipeDao.findByIds(
                        rows.stream().map(Comment::getRecipeId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(Recipe::getId, Function.identity()));
        return rows.stream()
                .map(comment -> CommentResponse.from(comment))
                .toList();
    }

    public List<AdminReportRow> reports() {
        return reportDao.findPending().stream()
                .map(this::toReportRow)
                .toList();
    }

    public void dismissReport(String reportId) {
        Report report = reportDao.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found."));
        reportDao.updateStatus(report.getId(), "DISMISSED");
    }

    private AdminReportRow toReportRow(Report report) {
        String reporter = userDao.findById(report.getReporterId())
                .map(User::getUsername).orElse("[deleted]");
        String targetTitle = null;
        if (report.getTargetType() == com.tastetribe.model.ReportTargetType.RECIPE) {
            targetTitle = recipeDao.findById(report.getTargetId()).map(Recipe::getTitle).orElse(null);
        } else {
            targetTitle = commentDao.findById(report.getTargetId()).map(Comment::getText).orElse(null);
        }
        return new AdminReportRow(report.getId(), report.getTargetType().json(), report.getTargetId(),
                report.getReason(), reporter, report.getStatus(), targetTitle,
                report.getCreatedAt() == null ? null : report.getCreatedAt().toInstant(java.time.ZoneOffset.UTC));
    }

    private static AdminRecipeRow toRecipeRow(Recipe recipe) {
        return new AdminRecipeRow(recipe.getId(), recipe.getTitle(), recipe.getAuthorUsername(),
                recipe.getStatus().json(), recipe.getViews(),
                recipe.getAvgRating() == null ? null
                        : Math.round(recipe.getAvgRating() * 100.0) / 100.0);
    }
}

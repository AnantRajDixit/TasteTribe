package com.tastetribe.service;

import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dao.ReviewDao;
import com.tastetribe.dto.SocialDtos.ReviewRequest;
import com.tastetribe.dto.SocialDtos.ReviewResponse;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Review;
import com.tastetribe.model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Ratings & reviews. Business rules:
 * - one review per user per recipe (unique constraint + upsert logic);
 * - the recipe's average rating is recomputed server-side on every change;
 * - a user may update their own review but cannot spam new ones.
 */
@Service
public class ReviewService {

    private final ReviewDao reviewDao;
    private final RecipeDao recipeDao;

    public ReviewService(ReviewDao reviewDao, RecipeDao recipeDao) {
        this.reviewDao = reviewDao;
        this.recipeDao = recipeDao;
    }

    public List<ReviewResponse> list(String recipeId) {
        return reviewDao.findByRecipe(recipeId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    public ReviewResponse upsert(User user, String recipeId, ReviewRequest request) {
        requireRecipe(recipeId);
        LocalDateTime now = LocalDateTime.now();
        java.util.Optional<Review> existing = reviewDao.findByRecipeAndUser(recipeId, user.getId());
        if (existing.isPresent()) {
            reviewDao.update(recipeId, user.getId(), request.rating(), request.comment());
            Review review = reviewDao.findByRecipeAndUser(recipeId, user.getId()).orElseThrow();
            recompute(recipeId);
            return ReviewResponse.from(review);
        }
        Review review = new Review();
        review.setRecipeId(recipeId);
        review.setUserId(user.getId());
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setUpdatedAt(now);
        reviewDao.save(review);
        recompute(recipeId);
        Review fresh = reviewDao.findById(review.getId()).orElse(review);
        return ReviewResponse.from(fresh);
    }

    public void deleteMine(User user, String recipeId) {
        requireRecipe(recipeId);
        reviewDao.deleteForRecipe(recipeId, user.getId());
        recompute(recipeId);
    }

    /** Aggregate AVG(rating) via SQL, stored back on the recipe row. */
    private void recompute(String recipeId) {
        ReviewDao.RatingSummary summary = reviewDao.summariseForRecipe(recipeId);
        Double avg = summary.avg() == null ? null : Math.round(summary.avg() * 100.0) / 100.0;
        recipeDao.updateRating(recipeId, avg, (int) summary.count());
    }

    private void requireRecipe(String recipeId) {
        if (recipeDao.findById(recipeId).isEmpty()) {
            throw new NotFoundException("Recipe not found.");
        }
    }
}

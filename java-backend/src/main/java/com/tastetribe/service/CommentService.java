package com.tastetribe.service;

import com.tastetribe.dao.CommentDao;
import com.tastetribe.dao.RecipeDao;
import com.tastetribe.dto.SocialDtos.CommentRequest;
import com.tastetribe.dto.SocialDtos.CommentResponse;
import com.tastetribe.exception.ForbiddenException;
import com.tastetribe.exception.NotFoundException;
import com.tastetribe.model.Comment;
import com.tastetribe.model.Role;
import com.tastetribe.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

/** Comment business logic with owner/admin authorization. */
@Service
public class CommentService {

    private final CommentDao commentDao;
    private final RecipeDao recipeDao;

    public CommentService(CommentDao commentDao, RecipeDao recipeDao) {
        this.commentDao = commentDao;
        this.recipeDao = recipeDao;
    }

    public List<CommentResponse> list(String recipeId) {
        return commentDao.findByRecipe(recipeId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    public CommentResponse add(User user, String recipeId, CommentRequest request) {
        requireRecipe(recipeId);
        Comment comment = new Comment();
        comment.setRecipeId(recipeId);
        comment.setUserId(user.getId());
        comment.setText(request.text().trim());
        commentDao.save(comment);
        return CommentResponse.from(commentDao.findById(comment.getId()).orElse(comment));
    }

    public void delete(User user, String commentId) {
        Comment comment = commentDao.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found."));
        if (!comment.getUserId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Not allowed.");
        }
        commentDao.deleteById(commentId);
    }

    private void requireRecipe(String recipeId) {
        if (recipeDao.findById(recipeId).isEmpty()) {
            throw new NotFoundException("Recipe not found.");
        }
    }
}

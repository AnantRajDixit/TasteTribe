package com.tastetribe.controller;

import com.tastetribe.dto.SocialDtos.CommentRequest;
import com.tastetribe.dto.SocialDtos.CommentResponse;
import com.tastetribe.dto.SocialDtos.ReportRequest;
import com.tastetribe.dto.SocialDtos.ReviewRequest;
import com.tastetribe.dto.SocialDtos.ReviewResponse;
import com.tastetribe.model.Report;
import com.tastetribe.model.ReportTargetType;
import com.tastetribe.service.CommentService;
import com.tastetribe.service.ReviewService;
import com.tastetribe.dao.ReportDao;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** Reviews (1-5 stars + text), comments and content reports. */
@RestController
public class SocialController {

    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ReportDao reportDao;
    private final SessionContext session;

    public SocialController(ReviewService reviewService, CommentService commentService,
                            ReportDao reportDao, SessionContext session) {
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.reportDao = reportDao;
        this.session = session;
    }

    // ---------- reviews ----------

    @GetMapping("/recipes/{id}/reviews")
    public List<ReviewResponse> reviews(@PathVariable String id) {
        return reviewService.list(id);
    }

    /** PUT = upsert: one review per user per recipe, updatable at any time. */
    @PutMapping("/recipes/{id}/reviews")
    public ReviewResponse upsertReview(@PathVariable String id, @Valid @RequestBody ReviewRequest body,
                                       HttpServletRequest request) {
        return reviewService.upsert(session.require(request), id, body);
    }

    @DeleteMapping("/recipes/{id}/reviews")
    public Map<String, Object> deleteReview(@PathVariable String id, HttpServletRequest request) {
        reviewService.deleteMine(session.require(request), id);
        return Map.of("ok", true);
    }

    // ---------- comments ----------

    @GetMapping("/recipes/{id}/comments")
    public List<CommentResponse> comments(@PathVariable String id) {
        return commentService.list(id);
    }

    @PostMapping("/recipes/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(@PathVariable String id,
                                                      @Valid @RequestBody CommentRequest body,
                                                      HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.add(session.require(request), id, body));
    }

    @DeleteMapping("/comments/{commentId}")
    public Map<String, Object> deleteComment(@PathVariable String commentId, HttpServletRequest request) {
        commentService.delete(session.require(request), commentId);
        return Map.of("ok", true);
    }

    // ---------- reports ----------

    @PostMapping("/reports")
    public ResponseEntity<Map<String, Object>> report(@Valid @RequestBody ReportRequest body,
                                                      HttpServletRequest request) {
        Report report = new Report();
        report.setTargetType(ReportTargetType.from(body.targetType()));
        report.setTargetId(body.targetId());
        report.setReason(body.reason().trim());
        report.setReporterId(session.require(request).getId());
        report.setStatus("PENDING");
        reportDao.save(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true, "id", report.getId()));
    }
}

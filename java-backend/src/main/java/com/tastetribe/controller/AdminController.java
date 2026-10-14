package com.tastetribe.controller;

import com.tastetribe.dto.AdminDtos.AdminRecipeRow;
import com.tastetribe.dto.AdminDtos.AdminReportRow;
import com.tastetribe.dto.AdminDtos.AdminUserRow;
import com.tastetribe.dto.AdminDtos.StatsResponse;
import com.tastetribe.dto.SocialDtos.CommentResponse;
import com.tastetribe.service.AdminService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Administrator dashboard — {@code /api/admin/*}. Every handler calls
 * {@link SessionContext#requireAdmin} first, so non-admins get a clean 403.
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final SessionContext session;

    public AdminController(AdminService adminService, SessionContext session) {
        this.adminService = adminService;
        this.session = session;
    }

    @GetMapping("/stats")
    public StatsResponse stats(HttpServletRequest request) {
        session.requireAdmin(request);
        return adminService.stats();
    }

    @GetMapping("/users")
    public List<AdminUserRow> users(@RequestParam(required = false) String q, HttpServletRequest request) {
        session.requireAdmin(request);
        return adminService.users(q);
    }

    @DeleteMapping("/users/{userId}")
    public Map<String, Object> deleteUser(@PathVariable String userId, HttpServletRequest request) {
        adminService.deleteUser(session.requireAdmin(request), userId);
        return Map.of("ok", true);
    }

    @GetMapping("/recipes")
    public List<AdminRecipeRow> recipes(@RequestParam(required = false) String q,
                                        @RequestParam(required = false) String status,
                                        HttpServletRequest request) {
        session.requireAdmin(request);
        return adminService.recipes(q, status);
    }

    @GetMapping("/comments")
    public List<CommentResponse> comments(HttpServletRequest request) {
        session.requireAdmin(request);
        return adminService.recentComments();
    }

    @GetMapping("/reports")
    public List<AdminReportRow> reports(HttpServletRequest request) {
        session.requireAdmin(request);
        return adminService.reports();
    }

    @PostMapping("/reports/{reportId}/dismiss")
    public Map<String, Object> dismiss(@PathVariable String reportId, HttpServletRequest request) {
        session.requireAdmin(request);
        adminService.dismissReport(reportId);
        return Map.of("ok", true);
    }
}

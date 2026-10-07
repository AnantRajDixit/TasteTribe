package com.tastetribe.web;

import com.tastetribe.exception.ForbiddenException;
import com.tastetribe.exception.UnauthorizedException;
import com.tastetribe.model.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Small helper controllers use to read the authenticated user that {@link AuthFilter}
 * resolved. Throws the appropriate custom exception when a stricter level is needed.
 */
@Component
public class SessionContext {

    /** Request attribute under which the authenticated user is stored. */
    public static final String ATTR = "tastetribe.currentUser";

    public Optional<User> current(HttpServletRequest request) {
        Object value = request.getAttribute(ATTR);
        return Optional.ofNullable(value).map(User.class::cast);
    }

    public User require(HttpServletRequest request) {
        return current(request)
                .orElseThrow(() -> new UnauthorizedException("You need to be logged in for that."));
    }

    public User requireAdmin(HttpServletRequest request) {
        User user = require(request);
        if (user.getRole() != com.tastetribe.model.Role.ADMIN) {
            throw new ForbiddenException("Admin access required.");
        }
        return user;
    }
}

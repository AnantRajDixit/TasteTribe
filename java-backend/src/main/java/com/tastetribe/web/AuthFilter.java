package com.tastetribe.web;

import com.tastetribe.dao.SessionDao;
import com.tastetribe.dao.UserDao;
import com.tastetribe.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Servlet integration: a classic {@link Filter} that resolves the {@code tt_session}
 * httpOnly cookie to the logged-in {@link User} and stores it as a request attribute.
 *
 * <p>Controllers then stay free of auth plumbing — they ask {@link SessionContext}
 * for the current user. Preflight (OPTIONS) requests pass through untouched.</p>
 */
@Component
public class AuthFilter implements Filter {

    private final SessionDao sessionDao;
    private final UserDao userDao;
    private final String cookieName;

    public AuthFilter(SessionDao sessionDao,
                      UserDao userDao,
                      @Value("${app.auth.cookie-name:tt_session}") String cookieName) {
        this.sessionDao = sessionDao;
        this.userDao = userDao;
        this.cookieName = cookieName;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        if (!"OPTIONS".equalsIgnoreCase(http.getMethod())) {
            resolveUser(http).ifPresent(user -> http.setAttribute(SessionContext.ATTR, user));
        }
        chain.doFilter(request, response);
    }

    private Optional<User> resolveUser(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) {
                return sessionDao.findSession(cookie.getValue())
                        .filter(session -> !session.isExpired())
                        .flatMap(session -> userDao.findById(session.getUserId()));
            }
        }
        return Optional.empty();
    }
}

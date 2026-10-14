package com.tastetribe.service;

import com.tastetribe.dao.SessionDao;
import com.tastetribe.dao.UserDao;
import com.tastetribe.dto.AuthDtos.ForgotPasswordRequest;
import com.tastetribe.dto.AuthDtos.ForgotPasswordResponse;
import com.tastetribe.dto.AuthDtos.LoginRequest;
import com.tastetribe.dto.AuthDtos.PasswordChangeRequest;
import com.tastetribe.dto.AuthDtos.RegisterRequest;
import com.tastetribe.dto.AuthDtos.ResetPasswordRequest;
import com.tastetribe.dto.AuthDtos.UpdateProfileRequest;
import com.tastetribe.dto.AuthDtos.UserResponse;
import com.tastetribe.exception.BadRequestException;
import com.tastetribe.exception.DuplicateResourceException;
import com.tastetribe.exception.UnauthorizedException;
import com.tastetribe.model.PasswordReset;
import com.tastetribe.model.Role;
import com.tastetribe.model.SessionToken;
import com.tastetribe.model.User;
import com.tastetribe.util.PasswordHasher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * Authentication business logic: registration (duplicate username/email prevention,
 * BCrypt hashing), login, logout, profile updates and the password flows.
 *
 * <p>Sessions are opaque random tokens stored server-side and delivered as httpOnly
 * cookies — the token is never exposed to JavaScript.</p>
 */
@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final PasswordHasher hasher;
    private final String cookieName;
    private final int sessionDays;

    public AuthService(UserDao userDao,
                       SessionDao sessionDao,
                       PasswordHasher hasher,
                       @Value("${app.auth.cookie-name:tt_session}") String cookieName,
                       @Value("${app.auth.session-days:7}") int sessionDays) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.hasher = hasher;
        this.cookieName = cookieName;
        this.sessionDays = sessionDays;
    }

    // ---------- registration / login ----------

    public UserResponse register(RegisterRequest request, HttpServletResponse response) {
        String username = request.username().toLowerCase();
        String email = request.email().toLowerCase();
        if (userDao.existsByUsername(username)) {
            throw new DuplicateResourceException("That username is already taken.");
        }
        if (userDao.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with that email already exists.");
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(hasher.hash(request.password()));
        user.setAvatarUrl(request.avatarUrl());
        user.setBio("");
        user.setRole(Role.USER);
        userDao.save(user);
        issueSession(user, response);
        return UserResponse.from(user, true);
    }

    public UserResponse login(LoginRequest request, HttpServletResponse response) {
        User user = userDao.findByIdentifier(request.usernameOrEmail().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password."));
        if (!hasher.verify(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password.");
        }
        issueSession(user, response);
        return UserResponse.from(user, true);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> token = cookieValue(request);
        token.ifPresent(sessionDao::deleteSession);
        response.addHeader("Set-Cookie", clearCookie().toString());
    }

    public UserResponse me(User user) {
        return UserResponse.from(user, true);
    }

    public UserResponse updateProfile(User current, UpdateProfileRequest request) {
        userDao.updateProfile(current.getId(),
                request.name().trim(),
                request.bio() == null ? "" : request.bio(),
                request.avatarUrl());
        User updated = userDao.findById(current.getId())
                .orElseThrow(() -> new BadRequestException("Account no longer exists."));
        return UserResponse.from(updated, true);
    }

    public void changePassword(User current, PasswordChangeRequest request, HttpServletRequest httpRequest) {
        if (!hasher.verify(request.currentPassword(), current.getPasswordHash())) {
            throw new BadRequestException("Your current password is incorrect.");
        }
        userDao.updatePassword(current.getId(), hasher.hash(request.newPassword()));
        cookieValue(httpRequest).ifPresent(keep ->
                sessionDao.deleteSessionsForUserExcept(current.getId(), keep));
    }

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {
        Optional<User> found = userDao.findByEmail(request.email().toLowerCase());
        if (found.isEmpty()) {
            // Never reveal whether an email is registered.
            return new ForgotPasswordResponse(true, null, null);
        }
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        PasswordReset reset = new PasswordReset(
                HexFormat.of().formatHex(bytes),
                found.get().getId(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1));
        sessionDao.saveResetToken(reset);
        // Demo environment: no mail provider attached, so the token is returned directly.
        // In production this token would be emailed as a link instead.
        return new ForgotPasswordResponse(true, reset.getToken(),
                "Reset token generated (valid 1 hour). Demo mode: no email is sent.");
    }

    public void resetPassword(ResetPasswordRequest request) {
        PasswordReset reset = sessionDao.findResetToken(request.token())
                .orElseThrow(() -> new BadRequestException("This reset link is invalid."));
        if (reset.isExpired()) {
            throw new BadRequestException("This reset link has expired.");
        }
        userDao.updatePassword(reset.getUserId(), hasher.hash(request.newPassword()));
        sessionDao.deleteResetToken(reset.getToken());
        sessionDao.deleteSessionsForUser(reset.getUserId());
    }

    // ---------- session helpers ----------

    private void issueSession(User user, HttpServletResponse response) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);
        LocalDateTime now = LocalDateTime.now();
        sessionDao.saveSession(new SessionToken(token, user.getId(), now, now.plusDays(sessionDays)));
        ResponseCookie cookie = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(sessionDays * 86400L)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private ResponseCookie clearCookie() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public Optional<String> cookieValue(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
                return Optional.of(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}

package com.tastetribe.controller;

import com.tastetribe.dto.AuthDtos.ForgotPasswordRequest;
import com.tastetribe.dto.AuthDtos.ForgotPasswordResponse;
import com.tastetribe.dto.AuthDtos.LoginRequest;
import com.tastetribe.dto.AuthDtos.PasswordChangeRequest;
import com.tastetribe.dto.AuthDtos.RegisterRequest;
import com.tastetribe.dto.AuthDtos.ResetPasswordRequest;
import com.tastetribe.dto.AuthDtos.UpdateProfileRequest;
import com.tastetribe.dto.AuthDtos.UserResponse;
import com.tastetribe.service.AuthService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints — {@code /api/auth/*}.
 *
 * <p>Thin controller: validation is declarative ({@code @Valid}), all rules live in
 * {@link AuthService}, and errors bubble up to the global exception handler.</p>
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionContext session;

    public AuthController(AuthService authService, SessionContext session) {
        this.authService = authService;
        this.session = session;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletResponse response) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request, response));
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        return authService.login(request, response);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return Map.of("ok", true);
    }

    /** Returns the logged-in user, or {@code null} when anonymous (never a 401). */
    @GetMapping("/me")
    public UserResponse me(HttpServletRequest request) {
        return session.current(request).map(authService::me).orElse(null);
    }

    @PatchMapping("/profile")
    public UserResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request,
                                      HttpServletRequest httpRequest) {
        return authService.updateProfile(session.require(httpRequest), request);
    }

    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@Valid @RequestBody PasswordChangeRequest request,
                                              HttpServletRequest httpRequest) {
        authService.changePassword(session.require(httpRequest), request, httpRequest);
        return Map.of("ok", true);
    }

    @PostMapping("/forgot-password")
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return Map.of("ok", true);
    }
}

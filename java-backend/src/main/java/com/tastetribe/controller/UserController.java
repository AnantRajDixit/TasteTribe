package com.tastetribe.controller;

import com.tastetribe.dto.AuthDtos.FollowResponse;
import com.tastetribe.dto.AuthDtos.ProfileResponse;
import com.tastetribe.dto.AuthDtos.UserResponse;
import com.tastetribe.dto.RecipeDtos.RecipeResponse;
import com.tastetribe.service.ProfileService;
import com.tastetribe.web.SessionContext;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Public chef profiles, their recipes, and the follow graph — {@code /api/users/*}. */
@RestController
@RequestMapping("/users")
public class UserController {

    private final ProfileService profileService;
    private final SessionContext session;

    public UserController(ProfileService profileService, SessionContext session) {
        this.profileService = profileService;
        this.session = session;
    }

    @GetMapping("/{username}")
    public ProfileResponse profile(@PathVariable String username, HttpServletRequest request) {
        return profileService.profile(username, session.current(request).orElse(null));
    }

    @GetMapping("/{username}/recipes")
    public List<RecipeResponse> recipes(@PathVariable String username,
                                        @RequestParam(required = false) String status,
                                        HttpServletRequest request) {
        return profileService.recipes(username, status, session.current(request).orElse(null));
    }

    @GetMapping("/{username}/followers")
    public List<UserResponse> followers(@PathVariable String username) {
        return profileService.followers(username);
    }

    @GetMapping("/{username}/following")
    public List<UserResponse> following(@PathVariable String username) {
        return profileService.following(username);
    }

    @PostMapping("/{username}/follow")
    public FollowResponse follow(@PathVariable String username, HttpServletRequest request) {
        return profileService.follow(session.require(request), username);
    }

    @DeleteMapping("/{username}/follow")
    public FollowResponse unfollow(@PathVariable String username, HttpServletRequest request) {
        return profileService.unfollow(session.require(request), username);
    }
}

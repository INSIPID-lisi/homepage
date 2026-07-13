package com.homepage.controller;

import com.homepage.dto.*;
import com.homepage.entity.User;
import com.homepage.security.annotation.RateLimit;
import com.homepage.security.annotation.RateLimitType;
import com.homepage.service.AuthService;
import com.homepage.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @RateLimit(key = "", windowSeconds = 300, maxRequests = 3, type = RateLimitType.IP_BASED)
    public Result<AuthResponse> register(@RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    @PostMapping("/login")
    @RateLimit(key = "", windowSeconds = 60, maxRequests = 10, type = RateLimitType.IP_BASED)
    public Result<AuthResponse> login(@RequestBody LoginRequest req) {
        return Result.success(authService.login(req));
    }

    @PostMapping("/login-code")
    public Result<AuthResponse> loginByCode(@RequestBody CodeLoginRequest req) {
        return Result.success(authService.loginByCode(req));
    }

    @PostMapping("/code")
    @RateLimit(key = "", windowSeconds = 60, maxRequests = 3, type = RateLimitType.IP_BASED)
    public Result<Void> sendCode(@RequestBody CodeLoginRequest req) {
        authService.sendCode(req.getEmail());
        return Result.success(null);
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordChangeRequest req) {
        authService.changePassword(req);
        return Result.success(null);
    }

    @GetMapping("/me")
    public Result<User> me() {
        User user = SecurityUtil.getCurrentUser();
        if (user == null) {
            return Result.error(401, "not authenticated");
        }
        user.setPassword(null);
        return Result.success(user);
    }
}

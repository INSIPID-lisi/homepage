package com.homepage.controller;

import com.homepage.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${app.security.admin-ips}")
    private String adminIps;

    @GetMapping("/check")
    public Result<Boolean> check(HttpServletRequest request) {
        Set<String> allowed = Set.of(adminIps.split(","));
        boolean admin = allowed.contains(request.getRemoteAddr());
        return Result.success(admin);
    }
}

package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.entity.User;
import com.homepage.util.SecurityUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/check")
    public Result<Boolean> check() {
        User user = SecurityUtil.getCurrentUser();
        return Result.success(user != null && "ADMIN".equals(user.getRole()));
    }
}

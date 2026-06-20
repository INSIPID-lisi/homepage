package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.entity.SocialLink;
import com.homepage.entity.UserProfile;
import com.homepage.service.SocialLinkService;
import com.homepage.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {

    private final UserProfileService userProfileService;
    private final SocialLinkService socialLinkService;

    @GetMapping("/profile")
    public Result<UserProfile> getProfile() {
        return Result.success(userProfileService.getProfile());
    }

    @GetMapping("/social-links")
    public Result<List<SocialLink>> getSocialLinks() {
        return Result.success(socialLinkService.list());
    }
}

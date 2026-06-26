package com.homepage.service.impl;

import com.homepage.entity.UserProfile;
import com.homepage.mapper.UserProfileMapper;
import com.homepage.service.UserProfileService;
import com.homepage.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getProfile() {
        List<UserProfile> list = userProfileMapper.selectList(null);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public void saveOrUpdateProfile(UserProfile profile) {
        SecurityUtil.requireAdmin();
        UserProfile existing = getProfile();
        if (existing != null) {
            profile.setId(existing.getId());
            userProfileMapper.updateById(profile);
        } else {
            profile.setId(null);
            userProfileMapper.insert(profile);
        }
    }
}

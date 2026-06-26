package com.homepage.service.impl;

import com.homepage.entity.SocialLink;
import com.homepage.mapper.SocialLinkMapper;
import com.homepage.service.SocialLinkService;
import com.homepage.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialLinkServiceImpl implements SocialLinkService {

    private final SocialLinkMapper socialLinkMapper;

    @Override
    public List<SocialLink> list() {
        return socialLinkMapper.selectList(null);
    }

    @Override
    public void updateBatch(List<SocialLink> links) {
        SecurityUtil.requireAdmin();
        socialLinkMapper.delete(null);
        for (SocialLink link : links) {
            link.setId(null);
            socialLinkMapper.insert(link);
        }
    }
}

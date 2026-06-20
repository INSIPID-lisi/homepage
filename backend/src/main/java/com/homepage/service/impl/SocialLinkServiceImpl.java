package com.homepage.service.impl;

import com.homepage.entity.SocialLink;
import com.homepage.mapper.SocialLinkMapper;
import com.homepage.service.SocialLinkService;
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
}

package com.homepage.service;

import com.homepage.entity.SocialLink;

import java.util.List;

public interface SocialLinkService {
    List<SocialLink> list();
    void updateBatch(List<SocialLink> links);
}

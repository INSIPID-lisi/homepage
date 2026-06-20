package com.homepage.service.impl;

import com.homepage.dto.AnimeVO;
import com.homepage.entity.Anime;
import com.homepage.entity.AnimeReview;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.mapper.AnimeMapper;
import com.homepage.mapper.AnimeReviewMapper;
import com.homepage.service.AnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnimeServiceImpl implements AnimeService {

    private final AnimeMapper animeMapper;
    private final AnimeReviewMapper animeReviewMapper;

    @Value("${app.security.admin-ips}")
    private String adminIps;

    @Override
    public List<AnimeVO> list() {
        return animeMapper.selectAllWithLatestReview();
    }

    @Override
    public Anime getById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "anime not found");
        }
        return anime;
    }

    @Override
    public List<AnimeReview> getReviews(Long animeId) {
        getById(animeId);
        return animeReviewMapper.selectByAnimeId(animeId);
    }

    @Override
    public void create(Anime anime) {
        checkAdminIp();
        anime.setId(null);
        anime.setDeleted(false);
        anime.setCreatedAt(LocalDateTime.now());
        anime.setUpdatedAt(LocalDateTime.now());
        animeMapper.insert(anime);
    }

    @Override
    public void update(Long id, Anime anime) {
        checkAdminIp();
        Anime existing = getById(id);
        if (anime.getName() != null) existing.setName(anime.getName());
        if (anime.getCoverUrl() != null) existing.setCoverUrl(anime.getCoverUrl());
        if (anime.getDescription() != null) existing.setDescription(anime.getDescription());
        existing.setUpdatedAt(LocalDateTime.now());
        animeMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        checkAdminIp();
        getById(id);
        animeMapper.softDelete(id);
    }

    @Override
    public void createReview(Long animeId, AnimeReview review) {
        checkAdminIp();
        getById(animeId);
        review.setId(null);
        review.setAnimeId(animeId);
        review.setDeleted(false);
        review.setCreatedTime(LocalDateTime.now());
        animeReviewMapper.insert(review);
    }

    @Override
    public void updateReview(Long reviewId, AnimeReview review) {
        checkAdminIp();
        AnimeReview existing = animeReviewMapper.selectById(reviewId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "review not found");
        }
        if (review.getContent() != null) existing.setContent(review.getContent());
        animeReviewMapper.updateById(existing);
    }

    @Override
    public void deleteReview(Long reviewId) {
        checkAdminIp();
        AnimeReview existing = animeReviewMapper.selectById(reviewId);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "review not found");
        }
        animeReviewMapper.softDelete(reviewId);
    }

    private void checkAdminIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "no request context");
        }
        String ip = attrs.getRequest().getRemoteAddr();
        Set<String> allowed = Set.of(adminIps.split(","));
        if (!allowed.contains(ip) && !allowed.contains("::ffff:" + ip)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "forbidden: ip not allowed");
        }
    }
}

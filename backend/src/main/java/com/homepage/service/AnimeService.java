package com.homepage.service;

import com.homepage.dto.AnimeVO;
import com.homepage.entity.Anime;
import com.homepage.entity.AnimeReview;

import java.util.List;

public interface AnimeService {
    List<AnimeVO> list();

    Anime getById(Long id);

    List<AnimeReview> getReviews(Long animeId);

    void create(Anime anime);

    void update(Long id, Anime anime);

    void delete(Long id);

    void createReview(Long animeId, AnimeReview review);

    void updateReview(Long reviewId, AnimeReview review);

    void deleteReview(Long reviewId);
}

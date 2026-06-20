package com.homepage.controller;

import com.homepage.dto.AnimeDetailVO;
import com.homepage.dto.AnimeVO;
import com.homepage.dto.Result;
import com.homepage.entity.Anime;
import com.homepage.entity.AnimeReview;
import com.homepage.service.AnimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anime")
@RequiredArgsConstructor
public class AnimeController {

    private final AnimeService animeService;

    @GetMapping
    public Result<List<AnimeVO>> list() {
        return Result.success(animeService.list());
    }

    @GetMapping("/{id}")
    public Result<AnimeDetailVO> getById(@PathVariable Long id) {
        Anime anime = animeService.getById(id);
        List<AnimeReview> reviews = animeService.getReviews(id);
        AnimeDetailVO vo = new AnimeDetailVO();
        BeanUtils.copyProperties(anime, vo);
        vo.setReviews(reviews);
        return Result.success(vo);
    }

    @PostMapping
    public Result<Void> create(@RequestBody Anime anime) {
        animeService.create(anime);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Anime anime) {
        animeService.update(id, anime);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        animeService.delete(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/reviews")
    public Result<Void> createReview(@PathVariable Long id, @RequestBody AnimeReview review) {
        animeService.createReview(id, review);
        return Result.success(null);
    }

    @PutMapping("/{id}/reviews/{reviewId}")
    public Result<Void> updateReview(@PathVariable Long id, @PathVariable Long reviewId, @RequestBody AnimeReview review) {
        animeService.updateReview(reviewId, review);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/reviews/{reviewId}")
    public Result<Void> deleteReview(@PathVariable Long id, @PathVariable Long reviewId) {
        animeService.deleteReview(reviewId);
        return Result.success(null);
    }
}

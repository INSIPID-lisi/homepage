package com.homepage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnimeVO {
    private Long id;
    private String name;
    private String coverUrl;
    private String description;
    private String latestReview;
    private LocalDateTime latestReviewTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

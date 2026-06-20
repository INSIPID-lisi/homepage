package com.homepage.dto;

import com.homepage.entity.AnimeReview;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AnimeDetailVO {
    private Long id;
    private String name;
    private String coverUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AnimeReview> reviews;
}

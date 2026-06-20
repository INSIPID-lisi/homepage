package com.homepage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("anime_review")
public class AnimeReview {
    private Long id;
    private Long animeId;
    private String content;
    private LocalDateTime createdTime;
    private Boolean deleted;
}

package com.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homepage.entity.AnimeReview;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AnimeReviewMapper extends BaseMapper<AnimeReview> {

    @Select("SELECT * FROM anime_review WHERE anime_id = #{animeId} AND deleted = 0 ORDER BY created_time DESC")
    List<AnimeReview> selectByAnimeId(@Param("animeId") Long animeId);

    @Select("SELECT * FROM anime_review WHERE id = #{id} AND deleted = 0")
    AnimeReview selectById(@Param("id") Long id);

    @Update("UPDATE anime_review SET deleted = 1 WHERE id = #{id}")
    void softDelete(@Param("id") Long id);
}

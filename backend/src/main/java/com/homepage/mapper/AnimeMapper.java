package com.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homepage.dto.AnimeVO;
import com.homepage.entity.Anime;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface AnimeMapper extends BaseMapper<Anime> {

    @Select("SELECT a.*, r.content AS latest_review, r.created_time AS latest_review_time " +
            "FROM anime a " +
            "LEFT JOIN anime_review r ON r.id = (" +
            "  SELECT r2.id FROM anime_review r2 WHERE r2.anime_id = a.id AND r2.deleted = 0 ORDER BY r2.created_time DESC LIMIT 1" +
            ") " +
            "WHERE a.deleted = 0 ORDER BY a.updated_at DESC")
    @Results({
        @Result(property = "id", column = "id"),
        @Result(property = "latestReview", column = "latest_review"),
        @Result(property = "latestReviewTime", column = "latest_review_time")
    })
    List<AnimeVO> selectAllWithLatestReview();

    @Select("SELECT * FROM anime WHERE id = #{id} AND deleted = 0")
    Anime selectById(@Param("id") Long id);

    @Update("UPDATE anime SET deleted = 1, updated_at = NOW() WHERE id = #{id}")
    void softDelete(@Param("id") Long id);
}

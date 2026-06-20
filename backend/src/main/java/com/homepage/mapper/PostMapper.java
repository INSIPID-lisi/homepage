package com.homepage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homepage.entity.Post;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PostMapper extends BaseMapper<Post> {

    @Select("SELECT * FROM post WHERE deleted = 0 ORDER BY pinned DESC, created_at DESC")
    List<Post> selectAllOrdered();

    @Select("SELECT * FROM post WHERE deleted = 0 AND type = #{type} ORDER BY pinned DESC, created_at DESC")
    List<Post> selectByType(@Param("type") String type);

    @Select("SELECT * FROM post WHERE deleted = 0 AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) ORDER BY pinned DESC, created_at DESC")
    List<Post> search(@Param("keyword") String keyword);

    @Select("SELECT * FROM post WHERE deleted = 0 AND type = #{type} AND (title LIKE CONCAT('%', #{keyword}, '%') OR content LIKE CONCAT('%', #{keyword}, '%')) ORDER BY pinned DESC, created_at DESC")
    List<Post> searchByType(@Param("type") String type, @Param("keyword") String keyword);

    @Select("SELECT * FROM post WHERE id = #{id} AND deleted = 0")
    Post selectById(@Param("id") Long id);

    @Update("UPDATE post SET deleted = 1, updated_at = NOW() WHERE id = #{id}")
    void softDelete(@Param("id") Long id);
}

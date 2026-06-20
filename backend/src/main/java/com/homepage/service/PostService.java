package com.homepage.service;

import com.homepage.entity.Post;

import java.util.List;

public interface PostService {
    List<Post> list(String type, String search);

    Post getById(Long id);

    void create(Post post);

    void update(Long id, Post post);

    void delete(Long id);
}

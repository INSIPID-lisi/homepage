package com.homepage.controller;

import com.homepage.dto.Result;
import com.homepage.entity.Post;
import com.homepage.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public Result<List<Post>> list(@RequestParam(required = false) String type,
                                    @RequestParam(required = false) String search) {
        return Result.success(postService.list(type, search));
    }

    @GetMapping("/{id}")
    public Result<Post> getById(@PathVariable Long id) {
        return Result.success(postService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Post post) {
        postService.create(post);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Post post) {
        postService.update(id, post);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        postService.delete(id);
        return Result.success(null);
    }
}

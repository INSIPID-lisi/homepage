package com.homepage.service.impl;

import com.homepage.entity.Post;
import com.homepage.exception.BusinessException;
import com.homepage.exception.ErrorCode;
import com.homepage.mapper.PostMapper;
import com.homepage.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;

    @Value("${app.security.admin-ips}")
    private String adminIps;

    @Override
    public List<Post> list(String type, String search) {
        boolean hasType = type != null && !type.isBlank();
        boolean hasSearch = search != null && !search.isBlank();

        if (hasType && hasSearch) {
            return postMapper.searchByType(type, search);
        }
        if (hasSearch) {
            return postMapper.search(search);
        }
        if (hasType) {
            return postMapper.selectByType(type);
        }
        return postMapper.selectAllOrdered();
    }

    @Override
    public Post getById(Long id) {
        Post post = postMapper.selectById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "post not found");
        }
        return post;
    }

    @Override
    public void create(Post post) {
        checkAdminIp();
        post.setId(null);
        post.setPinned(post.getPinned() != null && post.getPinned());
        post.setDeleted(false);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        postMapper.insert(post);
    }

    @Override
    public void update(Long id, Post post) {
        checkAdminIp();
        Post existing = getById(id);
        if (post.getTitle() != null) existing.setTitle(post.getTitle());
        if (post.getContent() != null) existing.setContent(post.getContent());
        if (post.getType() != null) existing.setType(post.getType());
        existing.setPinned(post.getPinned() != null && post.getPinned());
        existing.setUpdatedAt(LocalDateTime.now());
        postMapper.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        checkAdminIp();
        getById(id);
        postMapper.softDelete(id);
    }

    private void checkAdminIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "no request context");
        }

        String ip = attrs.getRequest().getRemoteAddr();
        Set<String> allowed = Set.of(adminIps.split(","));
        if (!allowed.contains(ip) && !allowed.contains("::ffff:" + ip)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "forbidden: ip not allowed");
        }
    }
}

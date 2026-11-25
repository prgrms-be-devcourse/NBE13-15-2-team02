package com.team02.catservice.post.dto;

import com.team02.catservice.post.entity.PostCategory;

import java.util.List;

// TODO : validation
public record PostCreateRequest(
        String title,
        String content,
        PostCategory category,
        String imageUrl,
        Long catId,
        List<String> tags
        ) {
}

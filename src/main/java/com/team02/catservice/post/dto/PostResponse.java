package com.team02.catservice.post.dto;

import com.team02.catservice.post.entity.Post;
import com.team02.catservice.post.entity.PostCategory;
import com.team02.catservice.post.entity.PostTag;
import com.team02.catservice.tag.entity.Tag;

import java.time.LocalDateTime;
import java.util.List;

public record PostResponse(
        Long postId,
        String title,
        String content,
        PostCategory category,
        String imageUrl,
        int likeCount,
        Long memberId,
        String nickname,
        Long catId,
        List<String> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static PostResponse from(Post post){
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategory(),
                post.getImageUrl(),
                post.getLikeCount(),
                post.getMember().getId(),
                post.getMember().getNickname(),
                post.getCat() == null ? null : post.getCat().getId(),
                post.getPostTags().stream()
                        .map(PostTag::getTag)
                        .map(Tag::getName)
                        .toList(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

}

package com.team02.catservice.post.dto;

import com.team02.catservice.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostCreateRequest(
        @NotBlank @Size(max = 100) String title,
        @NotBlank String content,
        @NotNull PostCategory category,
        @Size(max = 255) String imageUrl,
        Long catId,
        @Size(max = 10) List<@NotBlank @Size(max = 20) String> tags
        ) {
}

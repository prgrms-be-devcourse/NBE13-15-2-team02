package com.team02.catservice.post.dto;

import com.team02.catservice.post.entity.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PostUpdateRequest(
        @Pattern(regexp = ".*\\S.*", message = "제목은 공백만으로 작성할 수 없습니다.") @Size(max = 100) String title,
        @Pattern(regexp = "(?s).*\\S.*", message = "내용은 공백만으로 작성할 수 없습니다.") String content,
        PostCategory category,
        @Size(max = 255) String imageUrl,
        @Size(max = 10) List<@NotBlank @Size(max = 20) String> tags
) {
}

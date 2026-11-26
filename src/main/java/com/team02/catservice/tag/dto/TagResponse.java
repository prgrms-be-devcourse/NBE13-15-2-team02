package com.team02.catservice.tag.dto;

import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.entity.TagType;
import io.swagger.v3.oas.annotations.media.Schema;

// 태그 응답 DTO
@Schema(description = "태그 정보")
public record TagResponse(

        @Schema(example = "7")
        Long tagId,

        @Schema(description = "정규화된 태그 이름", example = "개냥이")
        String name,

        @Schema(description = "APPEARANCE(외모), PERSONALITY(성격), FREE(자유)", example = "PERSONALITY")
        TagType type
) {
    public static TagResponse from(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getType());
    }
}

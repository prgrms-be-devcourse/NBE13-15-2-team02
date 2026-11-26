package com.team02.catservice.tag.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "선호 태그 전체 교체 요청")
public record PreferredTagRequest(

        @ArraySchema(
                arraySchema = @Schema(description = "프리셋(외모·성격) 태그 ID 목록. 빈 배열이면 선호 태그를 모두 지웁니다."),
                schema = @Schema(example = "7")
        )
        @NotNull(message = "tagIds는 필수입니다. 모두 지우려면 빈 배열을 보내세요.")
        List<@NotNull(message = "태그 ID는 null일 수 없습니다.") Long> tagIds
) {
}

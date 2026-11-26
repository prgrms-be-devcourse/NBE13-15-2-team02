package com.team02.catservice.cat.dto;

import com.team02.catservice.cat.entity.Breed;
import com.team02.catservice.cat.entity.CatType;
import com.team02.catservice.cat.entity.Gender;
import com.team02.catservice.cat.entity.Neutered;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

/*
 * PATCH: null인 필드는 바꾸지 않고, 원래 값을 사용함.
 * 입양 상태는 여기서 바꿀 수 없음.
 */
// 고양이 수정 요청 DTO
@Schema(description = "고양이 수정 요청. null인 필드는 바뀌지 않습니다.")
public record CatUpdateRequest(

        @Schema(description = "이름", example = "나비")
        @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
        @Pattern(regexp = "(?s).*\\S.*", message = "이름은 공백일 수 없습니다.") // null은 통과
        String name,

        @Schema(description = "입양 공고가 열린 고양이는 STRAY로 바꿀 수 없습니다.", example = "RESCUED")
        CatType type,

        @Schema(example = "KOREAN_SHORTHAIR")
        Breed breed,

        @Schema(example = "FEMALE")
        Gender gender,

        @Schema(description = "추정 출생연도", example = "2024")
        Integer birthYear,

        @Schema(example = "YES")
        Neutered neutered,

        @Schema(description = "소개 글", example = "중성화 수술을 마쳤어요.")
        @Size(max = 500, message = "소개 글은 500자 이하여야 합니다.")
        String description,

        @Schema(description = "사진 주소", example = "https://example.com/cats/nabi.jpg")
        @Size(max = 255, message = "사진 주소는 255자 이하여야 합니다.")
        String imageUrl,

        @ArraySchema(
                arraySchema = @Schema(description = "null이면 유지, 빈 배열이면 모두 삭제, 값이 있으면 전체 교체"),
                schema = @Schema(example = "얌전함")
        )
        List<String> tags
) {
}

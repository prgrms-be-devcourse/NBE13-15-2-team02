package com.team02.catservice.cat.dto;

import com.team02.catservice.cat.entity.Breed;
import com.team02.catservice.cat.entity.CatType;
import com.team02.catservice.cat.entity.Gender;
import com.team02.catservice.cat.entity.Neutered;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

// 고양이 등록 요청 DTO
@Schema(description = "고양이 등록 요청")
public record CatCreateRequest(

        @Schema(description = "이름. 모르면 임시 이름", example = "나비")
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 30, message = "이름은 30자 이하여야 합니다.")
        String name,

        @Schema(description = "PET(반려묘), STRAY(길고양이), RESCUED(임시보호중)", example = "PET")
        @NotNull(message = "고양이 유형은 필수입니다.")
        CatType type,

        @Schema(description = "품종. 생략하면 UNKNOWN(모름)", example = "KOREAN_SHORTHAIR")
        Breed breed,

        @Schema(description = "성별. 생략하면 UNKNOWN(모름)", example = "FEMALE")
        Gender gender,

        @Schema(description = "추정 출생연도. 생략하면 '모름'", example = "2024", nullable = true)
        Integer birthYear,

        @Schema(description = "중성화 여부. 생략하면 UNKNOWN(모름)", example = "UNKNOWN")
        Neutered neutered,

        @Schema(description = "소개 글", example = "아파트 화단에서 구조했어요.")
        @Size(max = 500, message = "소개 글은 500자 이하여야 합니다.")
        String description,

        @Schema(description = "사진 주소", example = "https://example.com/cats/nabi.jpg")
        @Size(max = 255, message = "사진 주소는 255자 이하여야 합니다.")
        String imageUrl,

        @ArraySchema(
                arraySchema = @Schema(description = "태그 이름 목록. 정규화 후 최대 10개이며, 프리셋에 없는 이름은 FREE 태그로 만들어집니다."),
                schema = @Schema(example = "개냥이")
        )
        List<@NotBlank(message = "태그 이름은 비어 있을 수 없습니다.") String> tags
) {
}

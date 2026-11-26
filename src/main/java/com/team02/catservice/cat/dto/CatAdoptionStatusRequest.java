package com.team02.catservice.cat.dto;

import com.team02.catservice.cat.entity.CatAdoptionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "입양 공고 열기·닫기 요청")
// 입양 공고 상태 요청 DTO
public record CatAdoptionStatusRequest(

        @Schema(description = "AVAILABLE(입양 공고 열기) 또는 NONE(입양 공고 닫기)",
                allowableValues = {"AVAILABLE", "NONE"}, example = "AVAILABLE")
        @NotNull(message = "입양 상태는 필수입니다.")
        CatAdoptionStatus adoptionStatus
) {
}

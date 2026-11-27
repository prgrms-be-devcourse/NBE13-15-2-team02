package com.team02.catservice.cat.dto;

import com.team02.catservice.cat.entity.Breed;
import com.team02.catservice.cat.entity.CatAdoptionStatus;
import com.team02.catservice.cat.entity.CatType;
import com.team02.catservice.cat.entity.Gender;
import io.swagger.v3.oas.annotations.Parameter;

// 고양이 검색 DTO
public record CatSearchCondition(

        @Parameter(description = "고양이 유형", example = "RESCUED")
        CatType type,

        @Parameter(description = "입양 상태", example = "AVAILABLE")
        CatAdoptionStatus adoptionStatus,

        @Parameter(description = "품종", example = "RAGDOLL")
        Breed breed,

        @Parameter(description = "성별", example = "FEMALE")
        Gender gender,

        @Parameter(description = "태그 이름(#·공백은 무시)", example = "개냥이")
        String tag
        ) {
}

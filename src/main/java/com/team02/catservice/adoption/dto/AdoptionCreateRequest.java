package com.team02.catservice.adoption.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdoptionCreateRequest(
        @NotNull(message = "고양이 번호는 필수입니다.")
        Long catId,

        @NotBlank(message = "신청 사유는 필수입니다.")
        @Size(max = 500, message = "신청 사유는 500자 이하로 입력해 주세요.")
        String message
) {
}

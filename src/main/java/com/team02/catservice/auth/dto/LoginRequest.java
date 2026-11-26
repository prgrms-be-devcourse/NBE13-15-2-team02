package com.team02.catservice.auth.dto;

import jakarta.validation.constraints.NotBlank;

// 로그인 요청 : 아이디 & 비번
public record LoginRequest (
        @NotBlank(message = "아이디는 필수입니다.")
        String loginId,

        @NotBlank(message = "비밀번호는 필수입니다.")
        String password
){
}

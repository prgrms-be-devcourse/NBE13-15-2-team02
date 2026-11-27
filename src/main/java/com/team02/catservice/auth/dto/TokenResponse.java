package com.team02.catservice.auth.dto;

// 로그인·재발급 성공 시 응답: Access Token(30분)과 Refresh Token(14일)
public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}

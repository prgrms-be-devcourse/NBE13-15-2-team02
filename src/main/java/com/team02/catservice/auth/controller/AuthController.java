package com.team02.catservice.auth.controller;

import com.team02.catservice.auth.dto.SignupRequest;
import com.team02.catservice.auth.service.AuthService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.member.dto.MemberResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED) // 성공하면 201 응답
    public ApiResponse<MemberResponse> signup(@Valid @RequestBody SignupRequest request) {
        MemberResponse response = authService.signup(request);
        return ApiResponse.created("회원가입이 완료되었습니다.", response);
    }
}

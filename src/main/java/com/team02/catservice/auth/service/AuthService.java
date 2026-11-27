package com.team02.catservice.auth.service;


import com.team02.catservice.auth.dto.LoginRequest;
import com.team02.catservice.auth.dto.SignupRequest;
import com.team02.catservice.auth.dto.TokenResponse;
import com.team02.catservice.auth.entity.RefreshToken;
import com.team02.catservice.auth.repository.RefreshTokenRepository;
import com.team02.catservice.global.security.JwtTokenProvider;
import com.team02.catservice.member.dto.MemberResponse;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public MemberResponse signup(SignupRequest request) {
        // memberService.register(...)로 가입하고, 돌아온 Member 받기
        Member member = memberService.register(
                request.loginId(),
                request.password(),
                request.nickname()
        );
        // MemberResponse.from(...)으로 바꿔서 return
        return MemberResponse.from(member);
    }

    // 로그인 : 회원 확인 -> 토큰 2개 발급 -> Refresh Token 저장(있을 시 교체) -> 토큰 응답
    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 아이디 & 비번 확인 (틀리면 401)
        Member member = memberService.authenticate(request.loginId(), request.password());

        // 토큰 2개 생성
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpirationSeconds());

        // Refresh Token 저장 또는 있을 시 교체
        Optional<RefreshToken> saved = refreshTokenRepository.findByMemberId(member.getId());
        if (saved.isPresent()) {
            saved.get().rotate(refreshToken, expiresAt);
        } else {
            refreshTokenRepository.save(new RefreshToken(member, refreshToken, expiresAt));
        }

        // 토큰 2개 응답
        return new TokenResponse(accessToken, refreshToken);

    }

}

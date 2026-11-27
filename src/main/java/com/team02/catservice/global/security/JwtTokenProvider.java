package com.team02.catservice.global.security;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/*
 * 로그인에 성공한 회원에게 발급할 JWT(Access Token, Refresh Token)를 생성하고 검증
 * - 입력: 회원 id(Long)
 * - 출력: 서명된 JWT 문자열
 * - 사용처: AuthService(로그인, 재발급), JwtAuthenticationFilter(토큰 검증)
 * - 토큰 검증: getMemberIdFromAccessToken
 */
@Component // Spring Bean으로 등록 → AuthService 등에 생성자 주입 가능
public class JwtTokenProvider {

    // JWT 서명에 쓰는 HMAC-SHA 비밀 키 (위변조 검증 기준)
    private final SecretKey key;
    // Access Token 유효 기간(초). application.yaml: 1800 = 30분
    private final long accessExpirationSeconds;
    // Refresh Token 유효 기간(초). application.yaml: 1209600 = 14일
    private final long refreshExpirationSeconds;


    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,                                     // 서명용 비밀 문자열 (환경변수 JWT_SECRET)
            @Value("${jwt.access-expiration-seconds}") long accessExpirationSeconds,     // Access 유효 기간
            @Value("${jwt.refresh-expiration-seconds}") long refreshExpirationSeconds) { // Refresh 유효 기간
        // 비밀 문자열 → 바이트 배열 → HMAC-SHA 서명 키 객체 (32바이트 미만이면 예외)
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    // 회원 id로 Access Token(tokenType=access, 30분)을 생성
    public String createAccessToken(Long memberId) {
        return createToken(memberId, "access", accessExpirationSeconds);
    }

    // 회원 id로 Refresh Token(tokenType=refresh, 14일)을 생성
    public String createRefreshToken(Long memberId) {
        return createToken(memberId, "refresh", refreshExpirationSeconds);
    }

    //  Refresh Token 유효 기간(초)을 반환. DB에 만료 시각(expiresAt) 저장 시 사용
    public long getRefreshExpirationSeconds() {
        return refreshExpirationSeconds;
    }

    /*
     * JWT를 생성하는 공통 로직.
     * Payload(Claims)를 채운 뒤 비밀 키로 서명하고 문자열로 직렬화한다.
     */
    private String createToken(Long memberId, String tokenType, long expirationSeconds) {
        Instant now = Instant.now(); // 발급 기준 시각
        return Jwts.builder()                                              // 토큰 만들기 시작
                .subject(String.valueOf(memberId))                         // 토큰 주인: 회원 번호
                .claim("tokenType", tokenType)                             // 토큰 종류: access(30분) 또는 refresh(14일)
                .id(UUID.randomUUID().toString())                          // 토큰 고유 번호: 매번 다른 랜덤 값
                .issuedAt(Date.from(now))                                  // 발급 시각
                .expiration(Date.from(now.plusSeconds(expirationSeconds))) // 만료 시각: 발급 시각 + 유효 시간
                .signWith(key)                                             // 비밀 키로 서명 (내용을 고치면 검증에 실패함)
                .compact();                                                // 한 줄짜리 토큰 문자열로 완성
    }

    // Access Token을 검사하고 회원 번호를 꺼냄 (실패하면 401 예외)
    public Long getMemberIdFromAccessToken(String token) {
        Claims claims = parseClaims(token);                              // 도장·만료 검사
        if (!"access".equals(claims.get("tokenType", String.class))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);         // refresh 토큰이면 거절
        }
        return Long.valueOf(claims.getSubject());                        // 주인(회원 번호) 꺼내기
    }

    // 토큰을 열어서 도장과 만료 시각을 검사하고, 안에 적힌 내용(Claims)을 돌려줌
    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)            // 비밀 키로 도장 확인
                    .build()
                    .parseSignedClaims(token)   // 열기 (도장이 안 맞거나 만료면 예외)
                    .getPayload();              // 안에 적힌 내용 꺼내기
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);         // 만료
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);         // 위조·형식 오류·빈 값
        }
    }
}
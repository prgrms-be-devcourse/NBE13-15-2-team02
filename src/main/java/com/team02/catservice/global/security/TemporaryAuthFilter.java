package com.team02.catservice.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 임시 인증 필터: JwtAuthenticationFilter가 올라오기 전까지 모든 요청을 회원 ID 1로 인증한다.
 * 다른 팀원이 로그인 없이 개발할 수 있게 하는 용도이며, JWT 필터 적용 시 삭제한다.
 * X-Dev-Member-Id 헤더로 다른 회원 ID를 흉내 낼 수 있다. (권한 검증 테스트용)
 */
public class TemporaryAuthFilter extends OncePerRequestFilter {

    private static final long DEFAULT_MEMBER_ID = 1L;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        long memberId = parse(request.getHeader("X-Dev-Member-Id"));
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        chain.doFilter(request, response);
    }

    private long parse(String header) {
        try {
            return header == null ? DEFAULT_MEMBER_ID : Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return DEFAULT_MEMBER_ID;
        }
    }
}

package com.team02.catservice.global.security;

import com.team02.catservice.global.exception.BusinessException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// 요청의 "Authorization: Bearer 토큰"을 검사해서, 맞으면 몇 번 회원인지 등록하는 필터
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 토큰이 틀렸을 때 이유(에러 코드)를 요청에 붙여 두는 이름표. 401 담당이 읽어 감
    public static final String ERROR_CODE_ATTRIBUTE = "jwtErrorCode";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;   // Step 1에서 만든 토큰 검사기

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 1. 요청 헤더에서 "Authorization" 값 꺼내기 (예: "Bearer eyJhbGci...")
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        // 2. "Bearer "로 시작할 때만 검사 (없으면 그냥 통과 = 비로그인 상태)
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length());   // "Bearer " 뒤의 토큰만
            try {
                // 3. 토큰 검사 -> 회원 번호
                Long memberId = jwtTokenProvider.getMemberIdFromAccessToken(token);

                // 4. "이 요청은 memberId번 회원" 등록 (SecurityUtil이 여기서 꺼내 감)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        memberId, null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (BusinessException e) {
                // 5. 토큰이 틀렸으면 등록하지 않고, 이유만 이름표로 남김
                SecurityContextHolder.clearContext();
                request.setAttribute(ERROR_CODE_ATTRIBUTE, e.getErrorCode());
            }
        }

        // 6. 다음 단계로 넘기기 (공개 주소인지 or 로그인 필요 주소인지는 SecurityConfig가 판단)
        chain.doFilter(request, response);
    }
}
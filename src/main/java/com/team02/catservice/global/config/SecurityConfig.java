package com.team02.catservice.global.config;

import com.team02.catservice.global.security.JwtAccessDeniedHandler;
import com.team02.catservice.global.security.JwtAuthenticationEntryPoint;
import com.team02.catservice.global.security.JwtAuthenticationFilter;
import com.team02.catservice.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;                    // 토큰 검사기 (필터에 넘김)
    private final JwtAuthenticationEntryPoint authenticationEntryPoint; // 401 담당
    private final JwtAccessDeniedHandler accessDeniedHandler;           // 403 담당

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)          // 토큰 방식이라 CSRF 보호 불필요
                .formLogin(AbstractHttpConfigurer::disable)     // 기본 로그인 화면 끄기
                .httpBasic(AbstractHttpConfigurer::disable)     // 기본 팝업 로그인 끄기
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 안 씀

                // 어느 주소에 로그인이 필요한지 (위에서부터 처음 맞는 규칙 적용)
                .authorizeHttpRequests(auth -> auth
                        // 1. Swagger 문서, 에러 응답
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/error").permitAll()
                        // 2. 로그인 전에 쓰는 것
                        .requestMatchers(HttpMethod.POST,
                                "/api/v1/auth/signup", "/api/v1/auth/login", "/api/v1/auth/reissue").permitAll()
                        // 3. GET이어도 로그인이 필요한 것 (아래 "GET 공개"보다 먼저 있어야 함)
                        .requestMatchers(HttpMethod.GET, "/api/v1/members/me/**", "/api/v1/adoptions/**").authenticated()
                        // 4. 나머지 조회는 공개 (고양이, 태그, 게시글, 프로필 등)
                        .requestMatchers(HttpMethod.GET, "/api/v1/**").permitAll()
                        // 5. 그 외(생성·수정·삭제, 로그아웃 등)는 로그인 필요
                        .anyRequest().authenticated()
                )

                // 로그인 필요한데 인증 없음 → 401, 권한 부족 → 403
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 임시 필터 대신 JWT 문지기를 끼움
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
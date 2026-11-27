package com.team02.catservice.global.security;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

// 로그인이 필요한 주소에 인증 없이 들어왔을 때 401로 응답하는 담당
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 예외를 GlobalExceptionHandler로 넘겨 주는 창구 (팀 공통 에러 형식으로 응답하려고)
    private final HandlerExceptionResolver resolver;

    public JwtAuthenticationEntryPoint(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) {
        // 필터가 남긴 이름표가 있으면 그 이유로, 없으면 "로그인 필요"로
        Object attribute = request.getAttribute(JwtAuthenticationFilter.ERROR_CODE_ATTRIBUTE);
        ErrorCode errorCode = (attribute instanceof ErrorCode code) ? code : ErrorCode.UNAUTHORIZED;

        // GlobalExceptionHandler에게 넘겨서 {status, error, message, timestamp} 형식으로 응답
        resolver.resolveException(request, response, null, new BusinessException(errorCode));
    }
}
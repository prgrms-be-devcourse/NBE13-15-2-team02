package com.team02.catservice.global.security;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

// 로그인은 했지만 권한이 부족할 때 403으로 응답
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    // 예외를 GlobalExceptionHandler로 넘겨 주는 창구 (팀 공통 에러 형식으로 응답하려고)
    private final HandlerExceptionResolver resolver;

    public JwtAccessDeniedHandler(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) {
        // 403 FORBIDDEN으로 응답
        resolver.resolveException(request, response, null, new BusinessException(ErrorCode.FORBIDDEN));
    }
}
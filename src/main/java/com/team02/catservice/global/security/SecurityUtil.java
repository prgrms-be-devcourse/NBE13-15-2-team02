package com.team02.catservice.global.security;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 로그인한 회원의 ID를 꺼내는 유일한 방법. 인증 필터는 principal에 회원 ID(Long)를 넣는다.
 * 사용: Long memberId = SecurityUtil.getCurrentMemberId();
 */
public final class SecurityUtil {

    private SecurityUtil() {
    }

    /** 로그인이 필수인 곳에서 사용. 비로그인이면 401(UNAUTHORIZED). */
    public static Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long memberId)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return memberId;
    }

    /** 공개 API에서 로그인 여부에 따라 동작이 달라질 때 사용. 비로그인이면 null. */
    public static Long getCurrentMemberIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long memberId) {
            return memberId;
        }
        return null;
    }
}

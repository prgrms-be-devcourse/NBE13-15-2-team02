package com.team02.catservice.auth.repository;

import com.team02.catservice.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    // 회원 번호로 그 회원의 Refresh Token 찾기 (있으면 교체, 없으면 새로 저장)
    Optional<RefreshToken> findByMemberId(Long memberId);
}

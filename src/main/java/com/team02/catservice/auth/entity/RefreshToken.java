package com.team02.catservice.auth.entity;


import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 회원의 Refresh Token을 저장하는 엔티티 (회원당 1개)
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken extends BaseEntity {

    // 토큰 주인. 회원당 1개만 저장되도록 unique
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    // Refresh Token 문자열 (JWT는 길어서 넉넉하게)
    @Column(nullable = false, length = 500)
    private String token;

    // 만료 시각
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 처음 로그인할 때 새로 저장
    public RefreshToken(Member member, String token, LocalDateTime expiresAt) {
        this.member = member;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    // 다시 로그인하거나 재발급할 때 새 토큰으로 교체
    public void rotate(String token, LocalDateTime expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

}

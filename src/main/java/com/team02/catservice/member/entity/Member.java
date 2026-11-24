package com.team02.catservice.member.entity;

import com.team02.catservice.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {
    @Column(length = 20, unique = true)
    private String loginId;

    private String password;

    @Column(length = 20, nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Provider provider;

    private String providerId;

    public Member(String loginId, String password, String nickname) {
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.role = Role.USER;
        this.status = MemberStatus.ACTIVE;
        this.provider = Provider.LOCAL;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;

    }
    public void changePassword(String password) {
        this.password = password;
    }

    public void withdraw() {
        this.status = MemberStatus.WITHDRAWN;
    }

}
package com.team02.catservice.member.dto;

import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.entity.Role;

public record MemberResponse(
        Long id,
        String loginId,
        String nickname,
        Role role
) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getId(),
                member.getLoginId(),
                member.getNickname(),
                member.getRole()
        );
    }
}

package com.team02.catservice.auth.service;


import com.team02.catservice.auth.dto.SignupRequest;
import com.team02.catservice.member.dto.MemberResponse;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberService memberService;

    public MemberResponse signup(SignupRequest request) {
        // memberService.register(...)로 가입하고, 돌아온 Member 받기
        Member member = memberService.register(
                request.loginId(),
                request.password(),
                request.nickname()
        );
        // MemberResponse.from(...)으로 바꿔서 return
        return MemberResponse.from(member);

    }
}

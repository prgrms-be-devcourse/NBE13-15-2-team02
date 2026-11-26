package com.team02.catservice.member.service;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.entity.MemberStatus;
import com.team02.catservice.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public Member register(String loginId, String rawPassword, String nickname) {
        // 아이디 중복일 경우(409)
        if (memberRepository.existsByLoginId(loginId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        // 닉네임 중복일 경우(409)
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        // 비밀번호 암호화해서 Member 생성
        Member member = new Member(loginId, passwordEncoder.encode(rawPassword), nickname);

        // 저장 및 리턴
        return memberRepository.save(member);
    }

    // 로그인: 아이디·비밀번호가 맞는지 확인하고 회원을 돌려줌 (틀리면 401)
    public Member authenticate(String loginId, String rawPassword) {
        // 1. 아이디로 회원 찾기
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_FAILED));

        // 2. 탈퇴했거나, 비밀번호가 없거나(카카오 가입)
        if (member.getStatus() == MemberStatus.WITHDRAWN || member.getPassword() == null || !passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }
        // 3. 다 통과하면 회원 리턴
        return member;
    }
}

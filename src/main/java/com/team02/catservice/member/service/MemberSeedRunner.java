package com.team02.catservice.member.service;

import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberSeedRunner implements ApplicationRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (memberRepository.count() > 0) {
            return;
        }
        memberRepository.save(new Member("tester", passwordEncoder.encode("password1234"), "테스트회원1"));
        memberRepository.save(new Member("tester2", passwordEncoder.encode("password1234"), "테스트회원2"));
    }

}

package com.team02.catservice.cat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 고양이 품종
@Getter
@RequiredArgsConstructor
public enum Breed {
    KOREAN_SHORTHAIR("코리안 숏헤어"),
    RUSSIAN_BLUE("러시안 블루"),
    RAGDOLL("랙돌"),
    PERSIAN("페르시안"),
    SIAMESE("샴"),
    BRITISH_SHORTHAIR("브리티시 숏헤어"),
    AMERICAN_SHORTHAIR("아메리칸 숏헤어"),
    SCOTTISH_FOLD("스코티시 폴드"),
    MUNCHKIN("먼치킨"),
    BENGAL("벵갈"),
    MAINE_COON("메인쿤"),
    NORWEGIAN_FOREST("노르웨이 숲"),
    TURKISH_ANGORA("터키시 앙고라"),
    MIXED("믹스"),
    UNKNOWN("모름");

    private final String description;
}

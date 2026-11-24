package com.team02.catservice.cat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 입양 여부
@Getter
@RequiredArgsConstructor
public enum CatAdoptionStatus {
    NONE("입양 대상 아님"),
    AVAILABLE("입양 가능"),
    RESERVED("입양 진행 중"),
    ADOPTED("입양 완료");

    private final String description;
}

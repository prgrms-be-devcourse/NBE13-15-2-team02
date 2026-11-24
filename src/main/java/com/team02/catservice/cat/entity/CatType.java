package com.team02.catservice.cat.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

// 반려묘, 스트릿, 임보
@Getter
@RequiredArgsConstructor
public enum CatType {
    PET("반려묘"),
    STRAY("길고양이"),
    RESCUED("임시보호중");

    private final String description;
}

package com.team02.catservice.cat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 고양이 성별
@Getter
@RequiredArgsConstructor
public enum Gender {
    MALE("수컷"),
    FEMALE("암컷"),
    UNKNOWN("모름");

    private final String description;
}

package com.team02.catservice.cat.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 중성화 여부
@Getter
@RequiredArgsConstructor
public enum Neutered {
    YES("YES"),
    NO("NO"),
    UNKNOWN("모름");

    private final String description;
}

package com.team02.catservice.adoption.dto;

import com.team02.catservice.tag.entity.TagType;

/** [1] 점수 계산에 쓰는 태그. 이름과 종류(성격, 외모, 자유)가 모두 같아야 같은 태그로 본다 (26.11.25/안형빈) */
public record ScoringTag(String name, TagType type) {
}
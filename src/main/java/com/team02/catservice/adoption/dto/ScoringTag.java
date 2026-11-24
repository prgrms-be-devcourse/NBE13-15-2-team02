package com.team02.catservice.adoption.dto;

/** [1] 점수 계산에 쓰는 태그. Tag 엔티티가 올라오면 그쪽 타입으로 교체 예정 (26.11.24/안형빈) */
public record ScoringTag(String name, TagKind kind) {
}

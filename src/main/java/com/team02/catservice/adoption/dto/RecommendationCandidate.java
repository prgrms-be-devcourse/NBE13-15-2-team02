package com.team02.catservice.adoption.dto;

import java.time.LocalDateTime;
import java.util.Set;

/** [1] 추천 후보 고양이 한 마리의 정보. Cat과 그 태그를 이 형태로 바꿔서 추천 정렬에 넘긴다 (26.11.25/안형빈) */
public record RecommendationCandidate(
        Long catId,
        String name,
        LocalDateTime createdAt,
        Set<ScoringTag> tags
) {
}
package com.team02.catservice.adoption.dto;

/** [1] 후보 고양이 한 마리와 그 점수 결과를 묶는다. 정렬할 때 점수와 후보를 함께 들고 있기 위해 쓴다 (26.11.25/안형빈) */
public record Scored(RecommendationCandidate candidate, ScoreResult result) {
}
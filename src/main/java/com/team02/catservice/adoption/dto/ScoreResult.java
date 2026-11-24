package com.team02.catservice.adoption.dto;

import java.util.List;

/** [1] 계산 결과: 점수와 겹친 태그 이름 목록(추천 이유로 응답에 쓴다) (26.11.24/안형빈) */
public record ScoreResult(int score, List<String> matchedTags) {
}
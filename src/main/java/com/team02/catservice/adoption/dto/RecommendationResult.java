package com.team02.catservice.adoption.dto;

import java.util.List;

/** [1] 추천 결과. 추천 목록과, 회원에게 선호 태그가 있었는지를 함께 돌려준다. 선호 태그가 없으면 컨트롤러가 설정 안내 문구를 쓴다 (26.11.27/안형빈) */
public record RecommendationResult(
        List<RecommendedCatResponse> cats,
        boolean hasPreferredTags
) {
}
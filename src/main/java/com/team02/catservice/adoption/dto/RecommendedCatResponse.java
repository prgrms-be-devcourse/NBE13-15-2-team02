package com.team02.catservice.adoption.dto;

import java.util.List;

/** [1] 추천 목록의 고양이 한 마리. score와 matchedTags로 추천 이유를 보여 준다 (26.11.24/안형빈) */
public record RecommendedCatResponse(
        Long catId,
        String name,
        int score,
        List<String> matchedTags
) {
}
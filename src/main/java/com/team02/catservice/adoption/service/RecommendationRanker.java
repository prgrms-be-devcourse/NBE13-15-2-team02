package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.RecommendationCandidate;
import com.team02.catservice.adoption.dto.RecommendedCatResponse;
import com.team02.catservice.adoption.dto.Scored;
import com.team02.catservice.adoption.dto.ScoringTag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Component
public class RecommendationRanker {
    private final RecommendationScorer scorer;

    @Autowired
    public RecommendationRanker(RecommendationScorer scorer) {
        this.scorer = scorer;
    }

    /** [1] 후보 고양이들에 점수를 매겨 점수 높은 순, 같으면 최근 등록순으로 정렬하고 상위 size마리를 돌려준다 (26.11.25/안형빈) */
    public List<RecommendedCatResponse> rank(
            Set<ScoringTag> preferredTags,
            List<RecommendationCandidate> candidates,
            int size
    ) {
        /* [1-1] 후보가 없거나 size가 0 이하이면 빈 목록을 돌려준다. */
        if (candidates.isEmpty() || size <= 0) {
            return List.of();
        }

        return candidates.stream()
                /* [1-2] 후보마다 선호 태그와 겹치는 정도로 점수를 계산해 후보와 묶는다. (선호 태그가 없으면 모두 0점) */
                .map(candidate -> new Scored(candidate, scorer.calculate(preferredTags, candidate.tags())))
                /* [1-3] 점수 내림차순, 같으면 등록 시각 내림차순, 그래도 같으면 번호 내림차순으로 정렬한다. */
                .sorted(Comparator
                        .comparingInt((Scored s) -> s.result().score()).reversed()
                        .thenComparing((Scored s) -> s.candidate().createdAt(), Comparator.reverseOrder())
                        .thenComparing((Scored s) -> s.candidate().catId(), Comparator.reverseOrder()))
                /* [1-4] 위에서부터 size마리만 남기고 추천 응답으로 바꾼다. */
                .limit(size)
                .map(s -> new RecommendedCatResponse(
                        s.candidate().catId(),
                        s.candidate().name(),
                        s.result().score(),
                        s.result().matchedTags()))
                .toList();
    }
}
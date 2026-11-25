package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.ScoreResult;
import com.team02.catservice.adoption.dto.ScoringTag;
import com.team02.catservice.tag.entity.TagType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class RecommendationScorer {

    /** [1] 태그 종류별 가중치. 성격이 외모보다 크다 (26.11.24/안형빈) */
    private static final int PERSONALITY_WEIGHT = 3;
    private static final int APPEARANCE_WEIGHT = 2;

    /** [2] 선호 태그와 고양이 태그가 겹치는 만큼 점수를 계산한다 (26.11.24/안형빈) */
    public ScoreResult calculate(Set<ScoringTag> preferredTags, Set<ScoringTag> catTags) {
        /* [2-1] 선호 태그와 고양이 태그에서 겹치는 태그를 구한다. */
        List<ScoringTag> matchedTags = findMatchedTags(preferredTags, catTags);

        /* [2-2] 겹친 태그를 가중치에 맞는 점수로 바꿔 합산한다. */
        int score = matchedTags.stream()
                .mapToInt(tag -> weightOf(tag.type()))
                .sum();

        /* [2-3] 겹친 태그의 이름만 모은다. (추천 이유로 응답에 쓴다) */
        List<String> matchedTagNames = matchedTags.stream()
                .map(ScoringTag::name)
                .toList();

        return new ScoreResult(score, matchedTagNames);
    }

    /** [3] 이름과 종류가 모두 같은 태그만 겹친 것으로 본다 (26.11.24/안형빈) */
    private List<ScoringTag> findMatchedTags(Set<ScoringTag> preferredTags, Set<ScoringTag> catTags) {
        List<ScoringTag> matchedTags = new ArrayList<>();

        for (ScoringTag tag : preferredTags) {
            if (catTags.contains(tag)) {
                matchedTags.add(tag);
            }
        }
        /* 겹친 태그가 0개여도 빈 리스트를 돌려주면 된다. */
        return matchedTags;
    }

    /** [4] 태그 종류에 맞는 가중치를 돌려준다. 자유 태그는 0점 (26.11.24/안형빈) */
    private int weightOf(TagType type) {
        return switch (type) {
            case PERSONALITY -> PERSONALITY_WEIGHT;
            case APPEARANCE -> APPEARANCE_WEIGHT;
            case FREE -> 0;
        };
    }
}
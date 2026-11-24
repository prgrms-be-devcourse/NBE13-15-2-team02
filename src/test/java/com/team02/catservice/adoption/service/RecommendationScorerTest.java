package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.ScoreResult;
import com.team02.catservice.adoption.dto.ScoringTag;
import com.team02.catservice.adoption.dto.TagKind;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 점수 계산")
class RecommendationScorerTest {

    @Nested
    @DisplayName("명세서 계산 예 (선호 태그: 개냥이, 얌전함, 장모)")
    class SpecExample {

        @Test
        @DisplayName("나비는 6점이고 겹친 태그는 개냥이, 얌전함이다")
        void nabi_scores_6() {

            RecommendationScorer recommendationScorer = new RecommendationScorer();

            Set<ScoringTag> preferredTags = new HashSet<>();
            preferredTags.add(new ScoringTag("개냥이", TagKind.PERSONALITY));
            preferredTags.add(new ScoringTag("얌전함", TagKind.PERSONALITY));
            preferredTags.add(new ScoringTag("장모", TagKind.APPEARANCE));

            Set<ScoringTag> catTags = new HashSet<>();
            catTags.add(new ScoringTag("개냥이", TagKind.PERSONALITY));
            catTags.add(new ScoringTag("얌전함", TagKind.PERSONALITY));

            ScoreResult result = recommendationScorer.calculate(preferredTags, catTags);

            assertThat(result.score()).isEqualTo(6);
            assertThat(result.matchedTags()).containsExactlyInAnyOrder("개냥이", "얌전함");
        }

        @Test
        @DisplayName("치즈는 5점이고 겹친 태그는 개냥이, 장모이다")
        void cheese_scores_5() {
            RecommendationScorer recommendationScorer = new RecommendationScorer();

            Set<ScoringTag> preferredTags = new HashSet<>();
            preferredTags.add(new ScoringTag("개냥이", TagKind.PERSONALITY));
            preferredTags.add(new ScoringTag("얌전함", TagKind.PERSONALITY));
            preferredTags.add(new ScoringTag("장모", TagKind.APPEARANCE));

            Set<ScoringTag> catTags = new HashSet<>();
            catTags.add(new ScoringTag("개냥이", TagKind.PERSONALITY));
            catTags.add(new ScoringTag("장모", TagKind.APPEARANCE));

            ScoreResult result = recommendationScorer.calculate(preferredTags, catTags);

            assertThat(result.score()).isEqualTo(5);
            assertThat(result.matchedTags()).containsExactlyInAnyOrder("개냥이", "장모");
        }

        @Test
        @DisplayName("까미는 2점이고 겹친 태그는 장모이다")
        void kami_scores_2() {
            Set<ScoringTag> catTags = Set.of(
                    appearance("장모"), appearance("턱시도"), personality("활발함"));

            ScoreResult result = scorer.calculate(spec_preferred_tags(), catTags);

            assertThat(result.score()).isEqualTo(2);
            assertThat(result.matchedTags()).containsExactlyInAnyOrder("장모");
        }
    }

    @Nested
    @DisplayName("가중치")
    class Weight {

        @Test
        @DisplayName("성격 태그 하나는 3점이다")
        void personality_is_3() {
            ScoreResult result = scorer.calculate(Set.of(personality("개냥이")), Set.of(personality("개냥이")));

            assertThat(result.score()).isEqualTo(3);
        }

        @Test
        @DisplayName("외모 태그 하나는 2점이다")
        void appearance_is_2() {
            ScoreResult result = scorer.calculate(Set.of(appearance("장모")), Set.of(appearance("장모")));

            assertThat(result.score()).isEqualTo(2);
        }

        @Test
        @DisplayName("자유 태그가 겹쳐도 점수는 0점이다")
        void free_is_0() {
            ScoreResult result = scorer.calculate(Set.of(free("밥을빨리먹음")), Set.of(free("밥을빨리먹음")));

            assertThat(result.score()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("경계 케이스")
    class Boundary {

        @Test
        @DisplayName("겹치는 태그가 없으면 0점이고 겹친 태그 목록은 비어 있다")
        void no_match() {
            Set<ScoringTag> catTags = Set.of(personality("겁많음"), appearance("단모"));

            ScoreResult result = scorer.calculate(spec_preferred_tags(), catTags);

            assertThat(result.score()).isEqualTo(0);
            assertThat(result.matchedTags()).isEmpty();
        }

        @Test
        @DisplayName("선호 태그가 비어 있으면 0점이고 겹친 태그 목록은 비어 있다")
        void empty_preferred_tags() {
            Set<ScoringTag> catTags = Set.of(personality("개냥이"), appearance("장모"));

            ScoreResult result = scorer.calculate(Set.of(), catTags);

            assertThat(result.score()).isEqualTo(0);
            assertThat(result.matchedTags()).isEmpty();
        }

        @Test
        @DisplayName("고양이 태그가 비어 있으면 0점이고 겹친 태그 목록은 비어 있다")
        void empty_cat_tags() {
            ScoreResult result = scorer.calculate(spec_preferred_tags(), Set.of());

            assertThat(result.score()).isEqualTo(0);
            assertThat(result.matchedTags()).isEmpty();
        }

        @Test
        @DisplayName("이름은 같아도 종류가 다르면 겹친 태그가 아니다")
        void same_name_different_kind() {
            Set<ScoringTag> catTags = Set.of(personality("장모"));

            ScoreResult result = scorer.calculate(spec_preferred_tags(), catTags);

            assertThat(result.score()).isEqualTo(0);
            assertThat(result.matchedTags()).isEmpty();
        }

        @Test
        @DisplayName("겹친 태그 이름은 선호 태그 순서를 따른다")
        void matched_names_follow_preferred_order() {
            Set<ScoringTag> preferredTags = new LinkedHashSet<>();
            preferredTags.add(appearance("장모"));
            preferredTags.add(personality("얌전함"));
            preferredTags.add(personality("개냥이"));
            Set<ScoringTag> catTags = Set.of(personality("개냥이"), personality("얌전함"), appearance("장모"));

            ScoreResult result = scorer.calculate(preferredTags, catTags);

            assertThat(result.matchedTags()).containsExactly("장모", "얌전함", "개냥이");
        }
    }

    private final RecommendationScorer scorer = new RecommendationScorer();

    /** 명세서 계산 예의 선호 태그: 개냥이, 얌전함(성격), 장모(외모) */
    private Set<ScoringTag> spec_preferred_tags() {
        return Set.of(personality("개냥이"), personality("얌전함"), appearance("장모"));
    }

    private ScoringTag personality(String name) {
        return new ScoringTag(name, TagKind.PERSONALITY);
    }

    private ScoringTag appearance(String name) {
        return new ScoringTag(name, TagKind.APPEARANCE);
    }

    private ScoringTag free(String name) {
        return new ScoringTag(name, TagKind.FREE);
    }
}

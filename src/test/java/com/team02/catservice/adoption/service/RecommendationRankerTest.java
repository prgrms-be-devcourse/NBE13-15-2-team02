package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.RecommendationCandidate;
import com.team02.catservice.adoption.dto.RecommendedCatResponse;
import com.team02.catservice.adoption.dto.ScoringTag;
import com.team02.catservice.tag.entity.TagType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("추천 정렬")
class RecommendationRankerTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 25, 12, 0);

    private final RecommendationRanker ranker = new RecommendationRanker(new RecommendationScorer());

    /** 명세서 계산 예의 선호 태그: 개냥이, 얌전함(성격), 장모(외모) */
    private Set<ScoringTag> preferredTags() {
        return Set.of(personality("개냥이"), personality("얌전함"), appearance("장모"));
    }

    /** 나비: 개냥이, 얌전함, 단모 → 6점 */
    private RecommendationCandidate nabi(LocalDateTime createdAt) {
        return candidate(1L, "나비", createdAt, personality("개냥이"), personality("얌전함"), appearance("단모"));
    }

    /** 치즈: 개냥이, 장모, 치즈 → 5점 */
    private RecommendationCandidate cheese(LocalDateTime createdAt) {
        return candidate(2L, "치즈", createdAt, personality("개냥이"), appearance("장모"), appearance("치즈"));
    }

    /** 까미: 장모, 턱시도, 활발함 → 2점 */
    private RecommendationCandidate kami(LocalDateTime createdAt) {
        return candidate(3L, "까미", createdAt, appearance("장모"), appearance("턱시도"), personality("활발함"));
    }

    @Nested
    @DisplayName("점수 정렬")
    class SortByScore {

        @Test
        @DisplayName("점수가 높은 순으로 정렬되고 점수와 겹친 태그가 담긴다")
        void sorts_by_score_descending() {
            List<RecommendationCandidate> candidates = List.of(kami(BASE), nabi(BASE), cheese(BASE));

            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), candidates, 10);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("나비", "치즈", "까미");
            assertThat(result).extracting(RecommendedCatResponse::score).containsExactly(6, 5, 2);
            assertThat(result.get(0).matchedTags()).containsExactlyInAnyOrder("개냥이", "얌전함");
            assertThat(result.get(1).matchedTags()).containsExactlyInAnyOrder("개냥이", "장모");
            assertThat(result.get(2).matchedTags()).containsExactly("장모");
        }

        @Test
        @DisplayName("점수가 같으면 최근에 등록된 고양이가 먼저 나온다")
        void same_score_latest_first() {
            RecommendationCandidate older = candidate(10L, "먼저등록", BASE, personality("개냥이"));
            RecommendationCandidate newer = candidate(11L, "나중등록", BASE.plusHours(1), personality("개냥이"));

            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), List.of(older, newer), 10);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("나중등록", "먼저등록");
        }

        @Test
        @DisplayName("점수와 등록 시각이 모두 같으면 번호가 큰 고양이가 먼저 나온다")
        void same_score_same_time_larger_id_first() {
            RecommendationCandidate first = candidate(20L, "번호작음", BASE, personality("개냥이"));
            RecommendationCandidate second = candidate(21L, "번호큼", BASE, personality("개냥이"));

            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), List.of(first, second), 10);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("번호큼", "번호작음");
        }

        @Test
        @DisplayName("겹치는 태그가 없는 고양이도 점수 0으로 맨 뒤에 포함된다")
        void zero_score_included_last() {
            RecommendationCandidate noMatch = candidate(30L, "겹침없음", BASE.plusHours(5), appearance("고등어"));

            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), List.of(noMatch, kami(BASE)), 10);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("까미", "겹침없음");
            assertThat(result.get(1).score()).isZero();
            assertThat(result.get(1).matchedTags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("개수 제한")
    class Limit {

        @Test
        @DisplayName("size만큼만 돌려준다")
        void limits_to_size() {
            List<RecommendationCandidate> candidates = List.of(kami(BASE), nabi(BASE), cheese(BASE));

            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), candidates, 2);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("나비", "치즈");
        }

        @Test
        @DisplayName("후보가 size보다 적으면 있는 만큼만 돌려준다")
        void returns_all_when_fewer_than_size() {
            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), List.of(nabi(BASE)), 10);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("size가 0 이하이면 빈 목록을 돌려준다")
        void returns_empty_when_size_is_not_positive() {
            assertThat(ranker.rank(preferredTags(), List.of(nabi(BASE)), 0)).isEmpty();
            assertThat(ranker.rank(preferredTags(), List.of(nabi(BASE)), -1)).isEmpty();
        }
    }

    @Nested
    @DisplayName("경계 케이스")
    class Boundary {

        @Test
        @DisplayName("후보가 없으면 빈 목록을 돌려준다")
        void returns_empty_when_no_candidates() {
            List<RecommendedCatResponse> result = ranker.rank(preferredTags(), List.of(), 10);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("선호 태그가 없으면 점수 0으로 최근 등록순이 된다")
        void no_preferred_tags_latest_first() {
            List<RecommendationCandidate> candidates = List.of(
                    nabi(BASE),
                    cheese(BASE.plusHours(2)),
                    kami(BASE.plusHours(1)));

            List<RecommendedCatResponse> result = ranker.rank(Set.of(), candidates, 10);

            assertThat(result).extracting(RecommendedCatResponse::name).containsExactly("치즈", "까미", "나비");
            assertThat(result).extracting(RecommendedCatResponse::score).containsOnly(0);
            assertThat(result).allSatisfy(response -> assertThat(response.matchedTags()).isEmpty());
        }
    }

    private RecommendationCandidate candidate(Long catId, String name, LocalDateTime createdAt, ScoringTag... tags) {
        return new RecommendationCandidate(catId, name, createdAt, Set.of(tags));
    }

    private ScoringTag personality(String name) {
        return new ScoringTag(name, TagType.PERSONALITY);
    }

    private ScoringTag appearance(String name) {
        return new ScoringTag(name, TagType.APPEARANCE);
    }
}
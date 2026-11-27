package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.RecommendationResult;
import com.team02.catservice.adoption.dto.RecommendedCatResponse;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.entity.TagType;
import com.team02.catservice.tag.service.PreferredTagService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("추천 서비스 (getRecommendations)")
class RecommendationServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 11, 27, 12, 0);

    @Mock
    private CatService catService;

    @Mock
    private PreferredTagService preferredTagService;

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        recommendationService = new RecommendationService(
                catService,
                preferredTagService,
                adoptionRequestRepository,
                new RecommendationRanker(new RecommendationScorer()));
    }

    /** 명세서 계산 예의 선호 태그: 개냥이, 얌전함(성격), 장모(외모) */
    private List<TagResponse> preferredTags() {
        return List.of(
                new TagResponse(7L, "개냥이", TagType.PERSONALITY),
                new TagResponse(8L, "얌전함", TagType.PERSONALITY),
                new TagResponse(1L, "장모", TagType.APPEARANCE));
    }

    private Tag tag(String name, TagType type) {
        Tag tag = mock(Tag.class);
        lenient().when(tag.getName()).thenReturn(name);
        lenient().when(tag.getType()).thenReturn(type);
        return tag;
    }

    private Cat cat(Long id, String name, LocalDateTime createdAt, Tag... tags) {
        Cat cat = mock(Cat.class);
        lenient().when(cat.getId()).thenReturn(id);
        lenient().when(cat.getName()).thenReturn(name);
        lenient().when(cat.getCreatedAt()).thenReturn(createdAt);
        lenient().when(cat.getTags()).thenReturn(List.of(tags));
        return cat;
    }

    /** 나비: 개냥이, 얌전함, 단모 → 6점 */
    private Cat nabi() {
        return cat(12L, "나비", BASE, tag("개냥이", TagType.PERSONALITY), tag("얌전함", TagType.PERSONALITY),
                tag("단모", TagType.APPEARANCE));
    }

    /** 치즈: 개냥이, 장모, 치즈 → 5점 */
    private Cat cheese() {
        return cat(7L, "치즈", BASE, tag("개냥이", TagType.PERSONALITY), tag("장모", TagType.APPEARANCE),
                tag("치즈", TagType.APPEARANCE));
    }

    /** 까미: 장모, 턱시도, 활발함 → 2점 */
    private Cat kami() {
        return cat(3L, "까미", BASE, tag("장모", TagType.APPEARANCE), tag("턱시도", TagType.APPEARANCE),
                tag("활발함", TagType.PERSONALITY));
    }

    @Test
    @DisplayName("선호 태그와 겹치는 순서로 정렬되고 점수와 겹친 태그가 담긴다")
    void orders_by_score_with_matched_tags() {
        List<Cat> candidates = List.of(kami(), nabi(), cheese());
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(candidates);
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of());

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.hasPreferredTags()).isTrue();
        assertThat(result.cats()).extracting(RecommendedCatResponse::name).containsExactly("나비", "치즈", "까미");
        assertThat(result.cats()).extracting(RecommendedCatResponse::score).containsExactly(6, 5, 2);
        assertThat(result.cats().get(0).matchedTags()).containsExactlyInAnyOrder("개냥이", "얌전함");
        assertThat(result.cats().get(1).matchedTags()).containsExactlyInAnyOrder("개냥이", "장모");
    }

    @Test
    @DisplayName("내가 신청한 적이 있는 고양이는 추천에서 빠진다")
    void excludes_cats_already_applied_to() {
        List<Cat> candidates = List.of(nabi(), cheese(), kami());
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(candidates);
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of(7L));

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.cats()).extracting(RecommendedCatResponse::catId).containsExactly(12L, 3L);
    }

    @Test
    @DisplayName("선호 태그가 없으면 최근 등록순으로 돌려주고 선호 태그 없음을 알린다")
    void returns_latest_first_when_no_preferred_tags() {
        Cat older = cat(1L, "먼저", BASE.minusDays(2));
        Cat newest = cat(2L, "최근", BASE);
        Cat middle = cat(3L, "중간", BASE.minusDays(1));
        when(preferredTagService.getPreferredTags(1L)).thenReturn(List.of());
        when(catService.getRecommendationCandidates(1L)).thenReturn(List.of(older, newest, middle));
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of());

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.hasPreferredTags()).isFalse();
        assertThat(result.cats()).extracting(RecommendedCatResponse::name).containsExactly("최근", "중간", "먼저");
        assertThat(result.cats()).extracting(RecommendedCatResponse::score).containsOnly(0);
    }

    @Test
    @DisplayName("후보가 없으면 빈 목록을 돌려준다")
    void returns_empty_when_no_candidates() {
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(List.of());
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of());

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.cats()).isEmpty();
        assertThat(result.hasPreferredTags()).isTrue();
    }

    @Test
    @DisplayName("신청한 고양이를 빼고 남은 후보가 없으면 빈 목록을 돌려준다")
    void returns_empty_when_all_candidates_applied() {
        List<Cat> candidates = List.of(nabi());
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(candidates);
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of(12L));

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.cats()).isEmpty();
    }

    @Test
    @DisplayName("size만큼만 돌려준다")
    void limits_result_to_size() {
        List<Cat> candidates = List.of(kami(), nabi(), cheese());
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(candidates);
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of());

        RecommendationResult result = recommendationService.getRecommendations(1L, 2);

        assertThat(result.cats()).extracting(RecommendedCatResponse::name).containsExactly("나비", "치즈");
    }

    @Test
    @DisplayName("자유 태그는 점수에 반영되지 않는다")
    void free_tags_do_not_score() {
        Cat freeOnly = cat(1L, "자유냥", BASE, tag("개냥이", TagType.FREE));
        when(preferredTagService.getPreferredTags(1L)).thenReturn(preferredTags());
        when(catService.getRecommendationCandidates(1L)).thenReturn(List.of(freeOnly));
        when(adoptionRequestRepository.findCatIdsByApplicantId(1L)).thenReturn(List.of());

        RecommendationResult result = recommendationService.getRecommendations(1L, 10);

        assertThat(result.cats().get(0).score()).isZero();
        assertThat(result.cats().get(0).matchedTags()).isEmpty();
    }
}
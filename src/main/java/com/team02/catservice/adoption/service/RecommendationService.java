package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.RecommendationCandidate;
import com.team02.catservice.adoption.dto.RecommendationResult;
import com.team02.catservice.adoption.dto.RecommendedCatResponse;
import com.team02.catservice.adoption.dto.ScoringTag;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.tag.service.PreferredTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RecommendationService {
    private final CatService catService;
    private final PreferredTagService preferredTagService;
    private final AdoptionRequestRepository adoptionRequestRepository;
    private final RecommendationRanker recommendationRanker;

    @Autowired
    public RecommendationService(
            CatService catService,
            PreferredTagService preferredTagService,
            AdoptionRequestRepository adoptionRequestRepository,
            RecommendationRanker recommendationRanker
    ) {
        this.catService = catService;
        this.preferredTagService = preferredTagService;
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.recommendationRanker = recommendationRanker;
    }

    /** [1] 회원에게 맞는 고양이를 점수순으로 추천한다. 선호 태그가 없으면 최근 등록순이 된다 (26.11.27/안형빈) */
    public RecommendationResult getRecommendations(Long memberId, int size){
        /* [1-1] 내 선호 태그를 점수 계산용 태그(이름, 종류)로 바꿔 가져온다. */
        Set<ScoringTag> preferredTags = preferredTagService.getPreferredTags(memberId).stream()
                .map(tag -> new ScoringTag(tag.name(), tag.type()))
                .collect(Collectors.toSet());

        /* [1-2] 후보 고양이를 가져온다. (입양 가능하고 내가 등록하지 않은 고양이, 태그 포함) */
        List<Cat> cats = catService.getRecommendationCandidates(memberId);

        /* [1-3] 내가 신청한 적이 있는 고양이는 신청 상태와 무관하게 후보에서 뺀다. */
        Set<Long> appliedCatIds = new HashSet<>(adoptionRequestRepository.findCatIdsByApplicantId(memberId));

        /* [1-4] 남은 고양이를 추천 후보(번호, 이름, 등록 시각, 태그)로 바꾼다. */
        List<RecommendationCandidate> candidates = cats.stream()
                .filter(cat -> !appliedCatIds.contains(cat.getId()))
                .map(cat -> new RecommendationCandidate(
                        cat.getId(),
                        cat.getName(),
                        cat.getCreatedAt(),
                        cat.getTags().stream()
                                .map(tag -> new ScoringTag(tag.getName(), tag.getType()))
                                .collect(Collectors.toSet())))
                .toList();

        /* [1-5] 점수순(같으면 최근 등록순)으로 정렬해 size마리만 남긴다. */
        List<RecommendedCatResponse> recommendations = recommendationRanker.rank(preferredTags, candidates, size);

        /* [1-6] 추천 목록과 선호 태그 유무를 함께 돌려준다. (선호 태그가 없으면 안내 문구용) */
        return new RecommendationResult(recommendations, !preferredTags.isEmpty());
    }
}
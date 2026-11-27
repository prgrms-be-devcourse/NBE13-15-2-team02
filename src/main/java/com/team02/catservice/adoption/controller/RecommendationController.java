package com.team02.catservice.adoption.controller;

import com.team02.catservice.adoption.dto.RecommendationResult;
import com.team02.catservice.adoption.dto.RecommendedCatResponse;
import com.team02.catservice.adoption.service.RecommendationService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.security.SecurityUtil;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/adoptions/recommendations")
@Validated
public class RecommendationController {
    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /** [1] 회원에게 맞는 고양이를 점수순으로 추천한다. GET /api/v1/adoptions/recommendations (26.11.27/안형빈) */
    @GetMapping
    public ApiResponse<List<RecommendedCatResponse>> getRecommendations(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 50, message = "size는 50 이하여야 합니다.")
            int size
    ){
        /* [1-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [1-2] 서비스에서 추천 목록을 만든다. (선호 태그가 있었는지도 함께 받는다) */
        RecommendationResult result = recommendationService.getRecommendations(memberId, size);

        /* [1-3] 선호 태그가 없으면 설정을 안내하는 문구를, 있으면 기본 문구를 쓴다. */
        String message = result.hasPreferredTags()
                ? "추천 고양이 목록을 조회했습니다."
                : "선호 태그를 설정하면 취향에 맞는 고양이를 추천해 드립니다. 현재는 최근 등록순입니다.";

        /* [1-4] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok(message, result.cats());
    }
}
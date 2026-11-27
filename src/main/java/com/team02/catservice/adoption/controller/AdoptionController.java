package com.team02.catservice.adoption.controller;

import com.team02.catservice.adoption.dto.AdoptionCreateRequest;
import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.service.AdoptionService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.security.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/adoptions")
public class AdoptionController {
    private final AdoptionService adoptionService;

    @Autowired
    public AdoptionController(AdoptionService adoptionService) {
        this.adoptionService = adoptionService;
    }

    /** [1] 입양을 신청한다. POST /api/v1/adoptions (26.11.26/안형빈) */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AdoptionResponse> createAdoption(
            @Valid
            @RequestBody
            AdoptionCreateRequest request
    ){
        /* [1-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [1-2] 서비스에서 입양 신청을 만든다. */
        AdoptionResponse response = adoptionService.createAdoption(memberId, request);

        /* [1-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.created("입양 신청이 완료되었습니다.", response);
    }


    /** [2] 입양 신청을 승인한다. PATCH /api/v1/adoptions/{adoptionId}/approve (26.11.27/안형빈) */
    @PatchMapping("/{adoptionId}/approve")
    public ApiResponse<AdoptionResponse> approveAdoption(
            @PathVariable
            Long adoptionId
    ){
        /* [2-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [2-2] 서비스에서 입양 신청을 승인한다. */
        AdoptionResponse response = adoptionService.approveAdoption(memberId, adoptionId);

        /* [2-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("입양 신청을 승인했습니다.", response);
    }

    /** [3] 입양 신청을 거절한다. PATCH /api/v1/adoptions/{adoptionId}/reject (26.11.27/안형빈) */
    @PatchMapping("/{adoptionId}/reject")
    public ApiResponse<AdoptionResponse> rejectAdoption(
            @PathVariable
            Long adoptionId
    ){
        /* [3-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [3-2] 서비스에서 입양 신청을 거절한다. */
        AdoptionResponse response = adoptionService.rejectAdoption(memberId, adoptionId);

        /* [3-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("입양 신청을 거절했습니다.", response);
    }

    /** [4] 입양을 완료 처리한다. PATCH /api/v1/adoptions/{adoptionId}/complete (26.11.27/안형빈) */
    @PatchMapping("/{adoptionId}/complete")
    public ApiResponse<AdoptionResponse> completeAdoption(
            @PathVariable
            Long adoptionId
    ){
        /* [4-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [4-2] 서비스에서 입양을 완료 처리한다. */
        AdoptionResponse response = adoptionService.completeAdoption(memberId, adoptionId);

        /* [4-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("입양을 완료 처리했습니다.", response);
    }

    /** [5] 입양 신청을 취소한다. PATCH /api/v1/adoptions/{adoptionId}/cancel (26.11.27/안형빈) */
    @PatchMapping("/{adoptionId}/cancel")
    public ApiResponse<AdoptionResponse> cancelAdoption(
            @PathVariable
            Long adoptionId
    ){
        /* [5-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [5-2] 서비스에서 입양 신청을 취소한다. */
        AdoptionResponse response = adoptionService.cancelAdoption(memberId, adoptionId);

        /* [5-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("입양 신청을 취소했습니다.", response);
    }

    /** [6] 입양 신청 상세를 조회한다. GET /api/v1/adoptions/{adoptionId} (26.11.27/안형빈) */
    @GetMapping("/{adoptionId}")
    public ApiResponse<AdoptionResponse> getAdoption(
            @PathVariable
            Long adoptionId
    ){
        /* [6-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [6-2] 서비스에서 신청 상세를 조회한다. */
        AdoptionResponse response = adoptionService.getAdoption(memberId, adoptionId);

        /* [6-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("입양 신청을 조회했습니다.", response);
    }
}
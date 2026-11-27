package com.team02.catservice.adoption.controller;

import com.team02.catservice.adoption.dto.AdoptionCreateRequest;
import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.service.AdoptionService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.security.SecurityUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
}
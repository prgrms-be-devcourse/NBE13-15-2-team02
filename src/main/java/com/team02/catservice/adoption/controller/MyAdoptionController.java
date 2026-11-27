package com.team02.catservice.adoption.controller;

import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.service.AdoptionService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.response.PageResponse;
import com.team02.catservice.global.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members/me/adoptions")
public class MyAdoptionController {
    private final AdoptionService adoptionService;

    @Autowired
    public MyAdoptionController(AdoptionService adoptionService){
        this.adoptionService = adoptionService;
    }

    /** [1] 내가 보낸 입양 신청 목록을 최신순으로 조회한다. GET /api/v1/members/me/adoptions (26.11.25/안형빈) */
    @GetMapping
    public ApiResponse<PageResponse<AdoptionResponse>> getMyAdoptions(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        /* [1-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [1-2] 서비스에서 내가 보낸 신청을 페이징해서 가져온다. */
        PageResponse<AdoptionResponse> response = adoptionService.getMyAdoptions(memberId, pageable);

        /* [1-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("내가 보낸 입양 신청 목록을 조회했습니다.", response);
    }

    /** [2] 내 고양이에 들어온 입양 신청 목록을 최신순으로 조회한다. GET /api/v1/members/me/adoptions/received (26.11.27/안형빈) */
    @GetMapping("/received")
    public ApiResponse<PageResponse<AdoptionResponse>> getReceivedAdoptions(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        /* [2-1] 로그인한 회원의 번호를 꺼낸다. */
        Long memberId = SecurityUtil.getCurrentMemberId();

        /* [2-2] 서비스에서 내 고양이에 들어온 신청을 페이징해서 가져온다. */
        PageResponse<AdoptionResponse> response = adoptionService.getReceivedAdoptions(memberId, pageable);

        /* [2-3] 공통 응답 형식(status, message, data)으로 감싸 돌려준다. */
        return ApiResponse.ok("내 고양이에 들어온 입양 신청 목록을 조회했습니다.", response);
    }
}
package com.team02.catservice.cat.controller;

import com.team02.catservice.cat.dto.CatResponse;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MyPage의 내 고양이 (/api/v1/members/me/cats)
 */
@Tag(name = "My Cat", description = "내가 등록한 고양이 API")
@RestController
@RequestMapping("/api/v1/members/me/cats")
@RequiredArgsConstructor
public class MyCatController {

    private final CatService catService;

    /** 내가 등록한 고양이 목록: GET /members//me/cats (로그인) */
    @Operation(summary = "내가 등록한 고양이 목록")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CatResponse>>> getMyCats(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<CatResponse> data = PageResponse.from(catService.getMyCats(memberId, pageable));
        return ResponseEntity.ok(ApiResponse.ok("내 고양이 목록을 조회했습니다", data));
    }

}

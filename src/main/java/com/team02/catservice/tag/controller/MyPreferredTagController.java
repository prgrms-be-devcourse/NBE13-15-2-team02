package com.team02.catservice.tag.controller;

import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.tag.dto.PreferredTagRequest;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.service.PreferredTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 내 선호 태그 API (/api/v1/members/me/preferred-tags)
 * <p>
 * Tag 이름이 겹치므로, Swagger의 @Tag는 전체 경로로 적음.
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "My Preferred Tag", description = "내 선호 태그 API")
@RestController
@RequestMapping("/api/v1/members/me/preferred-tags")
@RequiredArgsConstructor
public class MyPreferredTagController {

    private final PreferredTagService preferredTagService;

    /** 내 선호 태그 조회: GET /members/me/preferred-tags (로그인) */
    @Operation(summary = "내 선호 태그 조회", description = "추천 목록 계산에 쓰이는 선호 태그입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getMyPreferredTags(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.ok("선호 태그를 조회했습니다.",
                preferredTagService.getMyPreferredTags(memberId)));
    }

    /**
     * 선호 태그 전체 교체: PUT /members/me/preferred-tags (로그인)
     * <p>
     * [PATCH가 아니라 PUT인 이유]
     * 일부를 더하거나 빼는 게 아니라 "보낸 목록으로 통째로 바꾼다"는 의미라서 PATCH 대신 PUT을 사용했다.
     * 같은 요청을 여러 번 보내도 결과가 같다(멱등). 빈 배열이면 선호 태그를 모두 지운다.
     * 새 리소스를 만드는 게 아니라 교체이므로 201이 아니라 200을 돌려준다.
     */
    @Operation(summary = "선호 태그 전체 교체",
            description = "프리셋(외모·성격) 태그만 설정할 수 있습니다. 빈 배열을 보내면 모두 지워집니다.")
    @PutMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> replacePreferredTags(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody PreferredTagRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("선호 태그를 수정했습니다.",
                preferredTagService.replacePreferredTags(memberId, request)));
    }

}

package com.team02.catservice.tag.controller;

import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.response.PageResponse;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.entity.TagType;
import com.team02.catservice.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Tag API (/api/v1/tags)
 * <p>
 * Tag 이름이 겹치므로, Swagger의 @Tag는 전체 경로로 적음.
 */
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tag", description = "태그 API")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 태그 목록: GET /tags (공개)
     * <p>
     * Tag의 경우에는 필터가 2개뿐(type, keyword)이라 별도 DTO 없이 @RequestParam으로 받는다.
     */
    @Operation(summary = "태그 목록",
            description = "type(APPEARANCE, PERSONALITY, FREE)과 keyword로 필터링합니다. "
                    + "선호 태그 설정 화면에서는 type=APPEARANCE, type=PERSONALITY로 프리셋만 불러오면 됩니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<TagResponse>>> getTags(
            @Parameter(description = "태그 종류", example = "PERSONALITY")
            @RequestParam(required = false) TagType type,
            @Parameter(description = "검색어 (#·공백은 무시)", example = "냥이")
            @RequestParam(required = false) String keyword,
            @ParameterObject @PageableDefault(sort = "id") Pageable pageable
    ) {
        PageResponse<TagResponse> data = PageResponse.from(tagService.getTags(type, keyword, pageable));
        return ResponseEntity.ok(ApiResponse.ok("태그 목록을 조회했습니다", data));
    }
}

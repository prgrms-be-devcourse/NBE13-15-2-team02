package com.team02.catservice.cat.controller;


import com.team02.catservice.cat.dto.*;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.global.response.ApiResponse;
import com.team02.catservice.global.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 고양이 API (/api/v1/cats)
 * <p>
 * &#064;Parameter(hidden  = true)는 Swagger 화면에 이 값을 입력 칸으로 보여 주지 않게 한다.
 */
@Tag(name = "Cat", description = "고양이 API")
@RestController
@RequestMapping("/api/v1/cats")
@RequiredArgsConstructor
public class CatController {

    private final CatService catService;

    /**
     * 고양이 등록: POST /cats (로그인)
     * <p>
     * 생성 201, HTTP 상태 코드 CREATED
     * <p>
     * 검증 실패시 400(INVALID_INPUT)
     */
    @Operation(summary = "고양이 등록",
            description = "tags는 이름으로 받습니다. 프리셋에 없는 이름은 FREE 태그로 만들어집니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CatResponse>> createCat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody CatCreateRequest request
    ) {
        CatResponse data = catService.createCat(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("고양이를 등록했습니다.", data));
    }

    /**
     * 고양이 목록: GET /cats (공개)
     * <p>
     * enum에 없는 값(?type=DOG)은 변환 단계에서 실패해 400으로 처리되어야 한다(팀원1 핸들러).
     * */
    @Operation(summary = "고양이 목록",
            description = "type, adoptionStatus, breed, gender, tag로 필터링합니다. 여러 개를 주면 AND로 묶습니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CatResponse>>> getCats(
            @ParameterObject @ModelAttribute CatSearchCondition condition,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        PageResponse<CatResponse> data = PageResponse.from(catService.getCats(condition, pageable));
        return ResponseEntity.ok(ApiResponse.ok("고양이 목록을 조회했습니다.", data));
    }

    /** 고양이 상세: GET /cats/{catId} (공개) */
    @Operation(summary = "고양이 상세")
    @GetMapping("/{catId}")
    public ResponseEntity<ApiResponse<CatResponse>> getCat(@PathVariable Long catId) {
        return ResponseEntity.ok(ApiResponse.ok("고양이를 조회했습니다.", catService.getCatDetail(catId)));
    }

    /** 고양이 수정: PATCH /cats/{catId} (본인) */
    @Operation(summary = "고양이 수정",
            description = "등록자 본인만 수정할 수 있습니다. 보내지 않은 필드(null)는 바뀌지 않으며, " +
                    "tags를 빈 배열로 보내면 태그가 모두 지워집니다.")
    @PatchMapping("/{catId}")
    public ResponseEntity<ApiResponse<CatResponse>> updateCat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long catId,
            @Valid @RequestBody CatUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok("고양이 정보를 수정했습니다.",
                catService.updateCat(memberId, catId, request)));
    }

    /**
     * 고양이 삭제: DELETE /cats/{catId} (본인)
     * <p>
     * 삭제는 204 대신 200과 메시지를 돌려줘 응답 모양을 유지한다. data는 null
     */
    @Operation(summary = "고양이 삭제", description = "진행 중인 입양 신청이 있으면 삭제할 수 없습니다. (409)")
    @DeleteMapping("/{catId}")
    public ResponseEntity<ApiResponse<Void>> deleteCat(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long catId
    ) {
        catService.deleteCat(memberId, catId);
        return ResponseEntity.ok(ApiResponse.ok("고양이를 삭제했습니다.", null));
    }

    /** 입양 공고 열기·닫기: PATCH /cats/{catId}/adoption-status (본인) */
    @Operation(summary = "입양 공고 열기·닫기",
            description = "NONE ↔ AVAILABLE만 가능합니다. 길고양이(STRAY)는 공고를 열 수 없고, "
                    + "입양 진행 중(RESERVED)에는 닫을 수 없습니다.")
    @PatchMapping("/{catId}/adoption-status")
    public ResponseEntity<ApiResponse<CatResponse>> changeAdoptionStatus(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
            @PathVariable Long catId,
            @Valid @RequestBody CatAdoptionStatusRequest request
            ) {
        return ResponseEntity.ok(ApiResponse.ok("입양 공고 상태를 변경했습니다.",
                catService.changeAdoptionStatus(memberId, catId, request)));
    }
}

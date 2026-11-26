package com.team02.catservice.cat.dto;

import com.team02.catservice.cat.entity.*;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.entity.MemberStatus;
import com.team02.catservice.tag.dto.TagResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

// 고양이 정보 응답 DTO
@Schema(description = "고양이 정보")
public record CatResponse(

        @Schema(example = "2")
        Long catId,

        @Schema(description = "등록자(보호자) ID", example = "7")
        Long ownerId,

        @Schema(description = "등록자(보호자) 닉네임. 탈퇴했으면, '탈퇴한 회원'", example = "고냥이")
        String ownerNickname,

        @Schema(example = "나비")
        String name,

        @Schema(example = "RESCUED")
        CatType type,

        @Schema(example = "RAGDOLL")
        Breed breed,

        @Schema(example = "FEMALE")
        Gender gender,

        @Schema(description = "추정 출생연도. null이면 모름", example = "2024", nullable = true)
        Integer birthYear,

        @Schema(description = "올해 기준 나이, null이면 모름", example = "2", nullable = true)
        Integer age,

        @Schema(example = "UNKNOWN")
        Neutered neutered,

        @Schema(example = "AVAILABLE")
        CatAdoptionStatus adoptionStatus,

        @Schema(example = "아파트 화단에서 구조했어요.", nullable = true)
        String description,

        @Schema(example = "https://example.com/cats/nabi.jpg", nullable = true)
        String imageUrl,

        @Schema(description = "고양이 태그 (type으로 프리셋·자유 태그 구분)")
        List<TagResponse> tags,

        @Schema(example = "2026-11-25T14:30:00")
        LocalDateTime createdAt
) {
    private static final String WITHDRAW_NICKNAME = "탈퇴한 회원";

    public static CatResponse from(Cat cat) {
        Member owner = cat.getMember();
        return new CatResponse(
                cat.getId(),
                owner.getId(),
                owner.getStatus() == MemberStatus.WITHDRAWN ? WITHDRAW_NICKNAME : owner.getNickname(),
                cat.getName(),
                cat.getType(),
                cat.getBreed(),
                cat.getGender(),
                cat.getBirthYear(),
                cat.getAge(),
                cat.getNeutered(),
                cat.getAdoptionStatus(),
                cat.getDescription(),
                cat.getImageUrl(),
                cat.getTags().stream().map(TagResponse::from).toList(),
                cat.getCreatedAt()

        );
    }
}

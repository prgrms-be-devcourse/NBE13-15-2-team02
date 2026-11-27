package com.team02.catservice.adoption.dto;

import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.member.entity.MemberStatus;

import java.time.LocalDateTime;

/** [1] 입양 신청 한 건의 응답. 신청 상세, 내가 보낸 신청, 내 고양이에 들어온 신청에 함께 쓴다 (26.11.24/안형빈) */
public record AdoptionResponse(
        Long adoptionId,
        Long catId,
        String catName,
        Long applicantId,
        String applicantNickname,
        String message,
        AdoptionRequestStatus status,
        LocalDateTime createdAt
) {
    /** [2] 입양 신청 엔티티 한 건을 응답 DTO로 바꾼다 (26.11.25/안형빈) */
    public static AdoptionResponse from(AdoptionRequest request){
        /* [2-1] 신청자가 탈퇴했으면 닉네임 대신 "탈퇴한 회원"으로 표시한다. */
        String applicantNickname = request.getApplicant().getStatus() == MemberStatus.WITHDRAWN
                ? "탈퇴한 회원"
                : request.getApplicant().getNickname();

        return new AdoptionResponse(
                request.getId(),
                request.getCat().getId(),
                request.getCat().getName(),
                request.getApplicant().getId(),
                applicantNickname,
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
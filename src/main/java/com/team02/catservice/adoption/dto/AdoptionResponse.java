package com.team02.catservice.adoption.dto;

import com.team02.catservice.adoption.entity.AdoptionRequestStatus;

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
}
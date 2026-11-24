package com.team02.catservice.cat.event;

/** [1] 고양이를 삭제하기 전에 발행. adoption이 받아서 진행 중인 신청이 있으면 예외로 막는다 (26.11.24/안형빈) */
public record CatDeletionRequestedEvent(Long catId) {
}

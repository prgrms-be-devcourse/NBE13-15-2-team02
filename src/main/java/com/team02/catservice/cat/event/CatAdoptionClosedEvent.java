package com.team02.catservice.cat.event;

/** [1] 입양 공고를 닫을 때 발행. adoption이 받아서 남은 REQUESTED 신청을 모두 거절한다 (26.11.24/안형빈) */
public record CatAdoptionClosedEvent(Long catId) {
}

package com.team02.catservice.cat.event;

/** [1] 고양이를 삭제할 때 발행. post가 받아서 연결된 글의 cat을 null로 바꾼다 (26.11.24/안형빈) */
public record CatDeletedEvent(Long catId) {
}

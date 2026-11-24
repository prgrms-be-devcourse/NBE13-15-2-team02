package com.team02.catservice.adoption.entity;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;

import java.util.Set;

public enum AdoptionRequestStatus {
    REQUESTED,
    APPROVED,
    COMPLETED,
    REJECTED,
    CANCELED;

    /** [1] 진행 중인 상태들의 집합 (26.11.24/안형빈) */
    private static final Set<AdoptionRequestStatus> IN_PROGRESS_STATUSES = Set.of(REQUESTED, APPROVED);

    /** [2] 행동별 허용 상태 집합 (승인/거절/완료/취소) (26.11.24/안형빈) */
    private static final Set<AdoptionRequestStatus> APPROVABLE_STATUSES = Set.of(REQUESTED);
    private static final Set<AdoptionRequestStatus> REJECTABLE_STATUSES = Set.of(REQUESTED, APPROVED);
    private static final Set<AdoptionRequestStatus> COMPLETABLE_STATUSES = Set.of(APPROVED);
    private static final Set<AdoptionRequestStatus> CANCELABLE_STATUSES = Set.of(REQUESTED, APPROVED);

    public AdoptionRequestStatus approve() {
        return transitionTo(APPROVED, APPROVABLE_STATUSES);
    }

    public AdoptionRequestStatus reject() {
        return transitionTo(REJECTED, REJECTABLE_STATUSES);
    }

    public AdoptionRequestStatus complete() {
        return transitionTo(COMPLETED, COMPLETABLE_STATUSES);
    }

    public AdoptionRequestStatus cancel() {
        return transitionTo(CANCELED, CANCELABLE_STATUSES);
    }

    public boolean isInProgress() {
        return IN_PROGRESS_STATUSES.contains(this);
    }

    /** [3] 공통 전이 검증 로직 (26.11.24/안형빈) */
    private AdoptionRequestStatus transitionTo(
            AdoptionRequestStatus targetStatus,
            Set<AdoptionRequestStatus> allowedCurrentStatuses
    ) {
        if (!allowedCurrentStatuses.contains(this)) {
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS);
        }
        return targetStatus;
    }
}
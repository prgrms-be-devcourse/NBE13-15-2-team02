package com.team02.catservice.adoption.entity;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;


@DisplayName("입양 신청 상태 전이")
class AdoptionRequestStatusTest {

    @Nested
    @DisplayName("승인")
    class Approve {

        @Test
        @DisplayName("REQUESTED에서 APPROVED가 된다")
        void requested_to_approved() {
            AdoptionRequestStatus status = AdoptionRequestStatus.REQUESTED;
            AdoptionRequestStatus result = status.approve();
            assertThat(result).isEqualTo(AdoptionRequestStatus.APPROVED);
        }

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"APPROVED", "COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("REQUESTED가 아닌 상태에서는 예외가 발생한다")
        void fails_when_not_requested(AdoptionRequestStatus status) {
            assertThatThrownBy(status::approve)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

    @Nested
    @DisplayName("거절")
    class Reject {

        @Test
        @DisplayName("REQUESTED에서 REJECTED가 된다")
        void requested_to_rejected() {
            AdoptionRequestStatus status = AdoptionRequestStatus.REQUESTED;
            AdoptionRequestStatus result = status.reject();
            assertThat(result).isEqualTo(AdoptionRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("APPROVED에서 REJECTED가 된다 (승인 철회)")
        void approved_to_rejected() {
            AdoptionRequestStatus status = AdoptionRequestStatus.APPROVED;
            AdoptionRequestStatus result = status.reject();
            assertThat(result).isEqualTo(AdoptionRequestStatus.REJECTED);
        }

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("끝난 상태에서는 예외가 발생한다")
        void fails_when_finished(AdoptionRequestStatus status) {
            assertThatThrownBy(status::reject)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

    @Nested
    @DisplayName("완료")
    class Complete {

        @Test
        @DisplayName("APPROVED에서 COMPLETED가 된다")
        void approved_to_completed() {
            AdoptionRequestStatus status = AdoptionRequestStatus.APPROVED;
            AdoptionRequestStatus result = status.complete();
            assertThat(result).isEqualTo(AdoptionRequestStatus.COMPLETED);
        }

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"REQUESTED", "COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("APPROVED가 아닌 상태에서는 예외가 발생한다")
        void fails_when_not_approved(AdoptionRequestStatus status) {
            assertThatThrownBy(status::complete)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

    @Nested
    @DisplayName("취소")
    class Cancel {

        @Test
        @DisplayName("REQUESTED에서 CANCELED가 된다")
        void requested_to_canceled() {
            AdoptionRequestStatus status = AdoptionRequestStatus.REQUESTED;
            AdoptionRequestStatus result = status.cancel();
            assertThat(result).isEqualTo(AdoptionRequestStatus.CANCELED);
        }

        @Test
        @DisplayName("APPROVED에서 CANCELED가 된다")
        void approved_to_canceled() {
            AdoptionRequestStatus status = AdoptionRequestStatus.APPROVED;
            AdoptionRequestStatus result = status.cancel();
            assertThat(result).isEqualTo(AdoptionRequestStatus.CANCELED);
        }

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("끝난 상태에서는 예외가 발생한다")
        void fails_when_finished(AdoptionRequestStatus status) {
            assertThatThrownBy(status::cancel)
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

    @Nested
    @DisplayName("끝난 상태")
    class Finished {

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("COMPLETED, REJECTED, CANCELED는 모든 행동이 예외를 던진다")
        void all_actions_fail(AdoptionRequestStatus status) {
            assertInvalidTransition(status::approve);
            assertInvalidTransition(status::reject);
            assertInvalidTransition(status::complete);
            assertInvalidTransition(status::cancel);
        }
    }

    @Nested
    @DisplayName("진행 중 여부")
    class InProgress {

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"REQUESTED", "APPROVED"})
        @DisplayName("REQUESTED, APPROVED는 진행 중이다")
        void in_progress(AdoptionRequestStatus status) {
            assertThat(status.isInProgress()).isTrue();
        }

        @ParameterizedTest
        @EnumSource(value = AdoptionRequestStatus.class, names = {"COMPLETED", "REJECTED", "CANCELED"})
        @DisplayName("COMPLETED, REJECTED, CANCELED는 진행 중이 아니다")
        void not_in_progress(AdoptionRequestStatus status) {
            assertThat(status.isInProgress()).isFalse();
        }
    }

    private void assertInvalidTransition(ThrowableAssert.ThrowingCallable action) {
        assertThatThrownBy(action)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
    }
}

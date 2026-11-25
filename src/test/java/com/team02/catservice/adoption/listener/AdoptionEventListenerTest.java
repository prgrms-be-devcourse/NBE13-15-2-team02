package com.team02.catservice.adoption.listener;

import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.event.CatAdoptionClosedEvent;
import com.team02.catservice.cat.event.CatDeletionRequestedEvent;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("입양 이벤트 리스너")
class AdoptionEventListenerTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @InjectMocks
    private AdoptionEventListener listener;

    @Nested
    @DisplayName("고양이 삭제 전 (CatDeletionRequestedEvent)")
    class CatDeletionRequested {

        @Test
        @DisplayName("진행 중인 신청이 있으면 INVALID_ADOPTION_STATUS 예외가 발생한다")
        void fails_when_in_progress_exists() {
            when(adoptionRequestRepository.existsByCatIdAndStatusIn(eq(1L), any())).thenReturn(true);

            assertThatThrownBy(() -> listener.handleCatDeletionRequested(new CatDeletionRequestedEvent(1L)))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
        }

        @Test
        @DisplayName("진행 중인 신청이 없으면 예외가 발생하지 않는다")
        void passes_when_no_in_progress() {
            when(adoptionRequestRepository.existsByCatIdAndStatusIn(eq(1L), any())).thenReturn(false);

            assertThatCode(()-> listener.handleCatDeletionRequested(new CatDeletionRequestedEvent(1L)))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("진행 중 상태 목록(REQUESTED, APPROVED)으로 조회한다")
        void queries_with_in_progress_statuses() {
            when(adoptionRequestRepository.existsByCatIdAndStatusIn(eq(1L), any())).thenReturn(false);

            listener.handleCatDeletionRequested(new CatDeletionRequestedEvent(1L));

            verify(adoptionRequestRepository).existsByCatIdAndStatusIn(1L, AdoptionRequestStatus.getInProgressStatuses());
        }
    }

    @Nested
    @DisplayName("공고 닫을 때 (CatAdoptionClosedEvent)")
    class CatAdoptionClosed {

        @Test
        @DisplayName("남은 REQUESTED 신청이 모두 REJECTED가 된다")
        void rejects_all_requested() {
            AdoptionRequest request1 = new AdoptionRequest(null, null, "사유1");
            AdoptionRequest request2 = new AdoptionRequest(null, null, "사유2");

            when(adoptionRequestRepository.findByCatIdAndStatus(eq(1L), eq(AdoptionRequestStatus.REQUESTED)))
                    .thenReturn(List.of(request1,request2));

            listener.handleCatAdoptionClosed(new CatAdoptionClosedEvent(1L));

            assertThat(request1.getStatus()).isEqualTo(AdoptionRequestStatus.REJECTED);
            assertThat(request2.getStatus()).isEqualTo(AdoptionRequestStatus.REJECTED);
        }

        @Test
        @DisplayName("남은 신청이 없으면 아무 일도 일어나지 않는다")
        void does_nothing_when_no_requests() {
            when(adoptionRequestRepository.findByCatIdAndStatus(eq(1L), eq(AdoptionRequestStatus.REQUESTED)))
                    .thenReturn(List.of());

            assertThatCode(() -> listener.handleCatAdoptionClosed(new CatAdoptionClosedEvent(1L)))
                    .doesNotThrowAnyException();

            verify(adoptionRequestRepository).findByCatIdAndStatus(1L, AdoptionRequestStatus.REQUESTED);
            verifyNoMoreInteractions(adoptionRequestRepository);
        }
    }
}
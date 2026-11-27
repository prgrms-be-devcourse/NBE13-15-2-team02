package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.AdoptionCreateRequest;
import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.entity.CatAdoptionStatus;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.global.response.PageResponse;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("입양 서비스")
class AdoptionServiceTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

    @Mock
    private CatService catService;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private AdoptionService adoptionService;

    @Nested
    @DisplayName("내가 보낸 신청 목록 (getMyAdoptions)")
    class GetMyAdoptions {

        @Test
        @DisplayName("신청자 기준으로 조회한 신청이 응답으로 변환된다")
        void converts_requests_to_responses() {
            Cat cat = mock(Cat.class);
            when(cat.getName()).thenReturn("나비");
            Member member = new Member("login", "pw", "nickname");
            AdoptionRequest request = new AdoptionRequest(cat, member, "사유");
            when(adoptionRequestRepository.findByApplicantId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(request)));

            PageResponse<AdoptionResponse> result = adoptionService.getMyAdoptions(1L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).catName()).isEqualTo("나비");
            assertThat(result.getContent().get(0).applicantNickname()).isEqualTo("nickname");
            assertThat(result.getContent().get(0).message()).isEqualTo("사유");
        }

        @Test
        @DisplayName("신청이 없으면 빈 목록을 돌려준다")
        void returns_empty_when_no_requests() {
            when(adoptionRequestRepository.findByApplicantId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            PageResponse<AdoptionResponse> result = adoptionService.getMyAdoptions(1L, PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("페이지 정보(page, size, totalElements, totalPages)가 담긴다")
        void includes_page_info() {
            Cat cat = mock(Cat.class);
            Member member = new Member("login", "pw", "nickname");
            AdoptionRequest request = new AdoptionRequest(cat, member, "사유");
            Pageable pageable = PageRequest.of(1, 5);
            when(adoptionRequestRepository.findByApplicantId(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(request), pageable, 12));

            PageResponse<AdoptionResponse> result = adoptionService.getMyAdoptions(1L, pageable);

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(12);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("입양 신청 (createAdoption)")
    class CreateAdoption {

        private final AdoptionCreateRequest request = new AdoptionCreateRequest(10L, "사유");

        @Test
        @DisplayName("조건을 만족하면 REQUESTED 상태의 신청이 저장되고 응답으로 돌려준다")
        void creates_requested_adoption() {
            Cat cat = mock(Cat.class);
            when(cat.getId()).thenReturn(10L);
            when(cat.getName()).thenReturn("나비");
            when(cat.isOwnedBy(1L)).thenReturn(false);
            when(cat.getAdoptionStatus()).thenReturn(CatAdoptionStatus.AVAILABLE);
            Member applicant = new Member("login", "pw", "nickname");
            when(catService.getCat(10L)).thenReturn(cat);
            when(adoptionRequestRepository.existsByCatIdAndApplicantIdAndStatusIn(
                    10L, 1L, AdoptionRequestStatus.getInProgressStatuses())).thenReturn(false);
            when(memberService.getMember(1L)).thenReturn(applicant);
            when(adoptionRequestRepository.save(any(AdoptionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

            AdoptionResponse response = adoptionService.createAdoption(1L, request);

            ArgumentCaptor<AdoptionRequest> captor = ArgumentCaptor.forClass(AdoptionRequest.class);
            verify(adoptionRequestRepository).save(captor.capture());
            AdoptionRequest saved = captor.getValue();
            assertThat(saved.getCat()).isSameAs(cat);
            assertThat(saved.getApplicant()).isSameAs(applicant);
            assertThat(saved.getMessage()).isEqualTo("사유");
            assertThat(saved.getStatus()).isEqualTo(AdoptionRequestStatus.REQUESTED);
            assertThat(response.catId()).isEqualTo(10L);
            assertThat(response.catName()).isEqualTo("나비");
            assertThat(response.message()).isEqualTo("사유");
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.REQUESTED);
        }

        @Test
        @DisplayName("고양이가 없으면 CAT_NOT_FOUND 예외가 발생하고 저장하지 않는다")
        void fails_when_cat_not_found() {
            when(catService.getCat(10L)).thenThrow(new BusinessException(ErrorCode.CAT_NOT_FOUND));

            assertThatThrownBy(() -> adoptionService.createAdoption(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.CAT_NOT_FOUND);
            verify(adoptionRequestRepository, never()).save(any(AdoptionRequest.class));
        }

        @Test
        @DisplayName("본인이 등록한 고양이에는 FORBIDDEN 예외가 발생하고 저장하지 않는다")
        void fails_when_own_cat() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(1L)).thenReturn(true);
            when(catService.getCat(10L)).thenReturn(cat);

            assertThatThrownBy(() -> adoptionService.createAdoption(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
            verify(adoptionRequestRepository, never()).save(any(AdoptionRequest.class));
        }

        @Test
        @DisplayName("본인 고양이이면서 입양 가능 상태가 아니어도 상태보다 먼저 FORBIDDEN이 발생한다")
        void checks_owner_before_status() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(1L)).thenReturn(true);
            when(catService.getCat(10L)).thenReturn(cat);

            assertThatThrownBy(() -> adoptionService.createAdoption(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("입양 가능한 상태가 아니면 INVALID_ADOPTION_STATUS 예외가 발생하고 저장하지 않는다")
        void fails_when_not_available() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(1L)).thenReturn(false);
            when(cat.getAdoptionStatus()).thenReturn(CatAdoptionStatus.RESERVED);
            when(catService.getCat(10L)).thenReturn(cat);

            assertThatThrownBy(() -> adoptionService.createAdoption(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.INVALID_ADOPTION_STATUS);
            verify(adoptionRequestRepository, never()).save(any(AdoptionRequest.class));
        }

        @Test
        @DisplayName("내 진행 중 신청이 이미 있으면 DUPLICATE_ADOPTION_REQUEST 예외가 발생하고 저장하지 않는다")
        void fails_when_duplicate_request() {
            Cat cat = mock(Cat.class);
            when(cat.getId()).thenReturn(10L);
            when(cat.isOwnedBy(1L)).thenReturn(false);
            when(cat.getAdoptionStatus()).thenReturn(CatAdoptionStatus.AVAILABLE);
            when(catService.getCat(10L)).thenReturn(cat);
            when(adoptionRequestRepository.existsByCatIdAndApplicantIdAndStatusIn(
                    10L, 1L, AdoptionRequestStatus.getInProgressStatuses())).thenReturn(true);

            assertThatThrownBy(() -> adoptionService.createAdoption(1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.DUPLICATE_ADOPTION_REQUEST);
            verify(adoptionRequestRepository, never()).save(any(AdoptionRequest.class));
        }
    }

    @Nested
    @DisplayName("입양 승인 (approveAdoption)")
    class ApproveAdoption {

        @Test
        @DisplayName("보호자가 승인하면 신청은 APPROVED가 되고 고양이는 예약 처리된다")
        void approves_and_reserves_cat() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            when(cat.getId()).thenReturn(10L);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            AdoptionResponse response = adoptionService.approveAdoption(2L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.APPROVED);
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.APPROVED);
            verify(catService).markReserved(10L);
        }

        @Test
        @DisplayName("신청이 없으면 ADOPTION_NOT_FOUND 예외가 발생한다")
        void fails_when_request_not_found() {
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.empty());

            assertErrorCode(() -> adoptionService.approveAdoption(2L, 5L), ErrorCode.ADOPTION_NOT_FOUND);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("보호자가 아니면 FORBIDDEN 예외가 발생하고 상태는 그대로다")
        void fails_when_not_owner() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(3L)).thenReturn(false);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.approveAdoption(3L, 5L), ErrorCode.FORBIDDEN);
            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.REQUESTED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("이미 끝난 신청이면 INVALID_ADOPTION_STATUS 예외가 발생하고 고양이를 건드리지 않는다")
        void fails_when_already_finished() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            AdoptionRequest request = requestOf(cat, 1L);
            request.reject();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.approveAdoption(2L, 5L), ErrorCode.INVALID_ADOPTION_STATUS);
            verifyNoInteractions(catService);
        }
    }

    @Nested
    @DisplayName("입양 거절 (rejectAdoption)")
    class RejectAdoption {

        @Test
        @DisplayName("승인된 신청을 거절하면 REJECTED가 되고 고양이는 다시 입양 가능으로 돌아간다")
        void rejects_approved_and_releases_cat() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            when(cat.getId()).thenReturn(10L);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            AdoptionResponse response = adoptionService.rejectAdoption(2L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.REJECTED);
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.REJECTED);
            verify(catService).markAvailable(10L);
        }

        @Test
        @DisplayName("승인 전(REQUESTED) 신청을 거절하면 REJECTED가 되고 고양이는 건드리지 않는다")
        void rejects_requested_without_touching_cat() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            adoptionService.rejectAdoption(2L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.REJECTED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("이미 끝난 신청이면 INVALID_ADOPTION_STATUS 예외가 발생하고 고양이를 건드리지 않는다")
        void fails_when_already_finished() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            AdoptionRequest request = requestOf(cat, 1L);
            request.cancel();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.rejectAdoption(2L, 5L), ErrorCode.INVALID_ADOPTION_STATUS);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("보호자가 아니면 FORBIDDEN 예외가 발생하고 상태는 그대로다")
        void fails_when_not_owner() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(3L)).thenReturn(false);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.rejectAdoption(3L, 5L), ErrorCode.FORBIDDEN);
            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.REQUESTED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("신청이 없으면 ADOPTION_NOT_FOUND 예외가 발생한다")
        void fails_when_request_not_found() {
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.empty());

            assertErrorCode(() -> adoptionService.rejectAdoption(2L, 5L), ErrorCode.ADOPTION_NOT_FOUND);
            verifyNoInteractions(catService);
        }
    }

    @Nested
    @DisplayName("입양 완료 (completeAdoption)")
    class CompleteAdoption {

        @Test
        @DisplayName("승인된 신청을 완료하면 COMPLETED가 되고 고양이는 입양 완료 처리되며 남은 신청은 거절된다")
        void completes_and_rejects_waiting_requests() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            when(cat.getId()).thenReturn(10L);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            AdoptionRequest waiting = requestOf(cat, 3L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));
            when(adoptionRequestRepository.findByCatIdAndStatus(10L, AdoptionRequestStatus.REQUESTED))
                    .thenReturn(List.of(waiting));

            AdoptionResponse response = adoptionService.completeAdoption(2L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.COMPLETED);
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.COMPLETED);
            assertThat(waiting.getStatus()).isEqualTo(AdoptionRequestStatus.REJECTED);
            verify(catService).markAdopted(10L);
        }

        @Test
        @DisplayName("남은 신청이 없어도 완료 처리된다")
        void completes_when_no_waiting_requests() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            when(cat.getId()).thenReturn(10L);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));
            when(adoptionRequestRepository.findByCatIdAndStatus(10L, AdoptionRequestStatus.REQUESTED))
                    .thenReturn(List.of());

            adoptionService.completeAdoption(2L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.COMPLETED);
            verify(catService).markAdopted(10L);
        }

        @Test
        @DisplayName("승인되지 않은 신청이면 INVALID_ADOPTION_STATUS 예외가 발생하고 아무것도 바꾸지 않는다")
        void fails_when_not_approved() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.completeAdoption(2L, 5L), ErrorCode.INVALID_ADOPTION_STATUS);
            verifyNoInteractions(catService);
            verify(adoptionRequestRepository, never()).findByCatIdAndStatus(any(), any());
        }

        @Test
        @DisplayName("보호자가 아니면 FORBIDDEN 예외가 발생하고 상태는 그대로다")
        void fails_when_not_owner() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(3L)).thenReturn(false);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.completeAdoption(3L, 5L), ErrorCode.FORBIDDEN);
            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.APPROVED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("신청이 없으면 ADOPTION_NOT_FOUND 예외가 발생한다")
        void fails_when_request_not_found() {
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.empty());

            assertErrorCode(() -> adoptionService.completeAdoption(2L, 5L), ErrorCode.ADOPTION_NOT_FOUND);
            verifyNoInteractions(catService);
        }
    }

    @Nested
    @DisplayName("입양 신청 취소 (cancelAdoption)")
    class CancelAdoption {

        @Test
        @DisplayName("승인된 신청을 취소하면 CANCELED가 되고 고양이는 다시 입양 가능으로 돌아간다")
        void cancels_approved_and_releases_cat() {
            Cat cat = mock(Cat.class);
            when(cat.getId()).thenReturn(10L);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            AdoptionResponse response = adoptionService.cancelAdoption(1L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.CANCELED);
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.CANCELED);
            verify(catService).markAvailable(10L);
        }

        @Test
        @DisplayName("승인 전(REQUESTED) 신청을 취소하면 CANCELED가 되고 고양이는 건드리지 않는다")
        void cancels_requested_without_touching_cat() {
            Cat cat = mock(Cat.class);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            adoptionService.cancelAdoption(1L, 5L);

            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.CANCELED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("입양 완료된 신청은 취소할 수 없다 (INVALID_ADOPTION_STATUS)")
        void fails_when_completed() {
            Cat cat = mock(Cat.class);
            AdoptionRequest request = requestOf(cat, 1L);
            request.approve();
            request.complete();
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.cancelAdoption(1L, 5L), ErrorCode.INVALID_ADOPTION_STATUS);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("신청자가 아니면 FORBIDDEN 예외가 발생하고 상태는 그대로다")
        void fails_when_not_applicant() {
            Cat cat = mock(Cat.class);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.cancelAdoption(3L, 5L), ErrorCode.FORBIDDEN);
            assertThat(request.getStatus()).isEqualTo(AdoptionRequestStatus.REQUESTED);
            verifyNoInteractions(catService);
        }

        @Test
        @DisplayName("신청이 없으면 ADOPTION_NOT_FOUND 예외가 발생한다")
        void fails_when_request_not_found() {
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.empty());

            assertErrorCode(() -> adoptionService.cancelAdoption(1L, 5L), ErrorCode.ADOPTION_NOT_FOUND);
            verifyNoInteractions(catService);
        }
    }

    @Nested
    @DisplayName("입양 신청 상세 (getAdoption)")
    class GetAdoption {

        @Test
        @DisplayName("신청자가 조회하면 신청이 응답으로 돌려진다")
        void applicant_can_view() {
            Cat cat = mock(Cat.class);
            when(cat.getName()).thenReturn("나비");
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            AdoptionResponse response = adoptionService.getAdoption(1L, 5L);

            assertThat(response.catName()).isEqualTo("나비");
            assertThat(response.applicantId()).isEqualTo(1L);
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.REQUESTED);
        }

        @Test
        @DisplayName("고양이의 보호자가 조회해도 된다")
        void owner_can_view() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(2L)).thenReturn(true);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            AdoptionResponse response = adoptionService.getAdoption(2L, 5L);

            assertThat(response.applicantId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("신청자도 보호자도 아니면 FORBIDDEN 예외가 발생한다")
        void fails_when_neither_applicant_nor_owner() {
            Cat cat = mock(Cat.class);
            when(cat.isOwnedBy(3L)).thenReturn(false);
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.of(request));

            assertErrorCode(() -> adoptionService.getAdoption(3L, 5L), ErrorCode.FORBIDDEN);
        }

        @Test
        @DisplayName("신청이 없으면 ADOPTION_NOT_FOUND 예외가 발생한다")
        void fails_when_request_not_found() {
            when(adoptionRequestRepository.findById(5L)).thenReturn(Optional.empty());

            assertErrorCode(() -> adoptionService.getAdoption(1L, 5L), ErrorCode.ADOPTION_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("내 고양이에 들어온 신청 목록 (getReceivedAdoptions)")
    class GetReceivedAdoptions {

        @Test
        @DisplayName("보호자 기준으로 조회한 신청이 응답으로 변환된다")
        void converts_requests_to_responses() {
            Cat cat = mock(Cat.class);
            when(cat.getName()).thenReturn("나비");
            AdoptionRequest request = requestOf(cat, 1L);
            when(adoptionRequestRepository.findByCatMemberId(eq(2L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(request)));

            PageResponse<AdoptionResponse> result = adoptionService.getReceivedAdoptions(2L, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).catName()).isEqualTo("나비");
            assertThat(result.getContent().get(0).applicantId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("들어온 신청이 없으면 빈 목록을 돌려준다")
        void returns_empty_when_no_requests() {
            when(adoptionRequestRepository.findByCatMemberId(eq(2L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of()));

            PageResponse<AdoptionResponse> result = adoptionService.getReceivedAdoptions(2L, PageRequest.of(0, 10));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("페이지 정보(page, size, totalElements, totalPages)가 담긴다")
        void includes_page_info() {
            Cat cat = mock(Cat.class);
            AdoptionRequest request = requestOf(cat, 1L);
            Pageable pageable = PageRequest.of(1, 5);
            when(adoptionRequestRepository.findByCatMemberId(eq(2L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(request), pageable, 12));

            PageResponse<AdoptionResponse> result = adoptionService.getReceivedAdoptions(2L, pageable);

            assertThat(result.getPage()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(12);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }

    private AdoptionRequest requestOf(Cat cat, Long applicantId) {
        Member applicant = new Member("login", "pw", "nickname");
        ReflectionTestUtils.setField(applicant, "id", applicantId);
        return new AdoptionRequest(cat, applicant, "사유");
    }

    private void assertErrorCode(ThrowingCallable callable, ErrorCode errorCode) {
        assertThatThrownBy(callable)
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(errorCode);
    }
}
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}
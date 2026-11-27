package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.global.response.PageResponse;
import com.team02.catservice.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("입양 서비스")
class AdoptionServiceTest {

    @Mock
    private AdoptionRequestRepository adoptionRequestRepository;

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
}
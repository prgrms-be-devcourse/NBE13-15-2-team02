package com.team02.catservice.adoption.dto;

import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("입양 신청 응답 변환 (AdoptionResponse.from)")
class AdoptionResponseTest {

    @Nested
    @DisplayName("값 옮기기")
    class MapFields {

        @Test
        @DisplayName("신청의 값이 응답으로 그대로 옮겨진다")
        void maps_all_fields() {
            Cat cat = mock(Cat.class);
            when(cat.getId()).thenReturn(1L);
            when(cat.getName()).thenReturn("나비");
            Member member = new Member("login", "pw", "nickname");
            ReflectionTestUtils.setField(member, "id", 2L);
            AdoptionRequest request = new AdoptionRequest(cat, member, "사유");
            LocalDateTime createdAt = LocalDateTime.of(2026, 11, 25, 12, 0);
            ReflectionTestUtils.setField(request, "id", 10L);
            ReflectionTestUtils.setField(request, "createdAt", createdAt);

            AdoptionResponse response = AdoptionResponse.from(request);

            assertThat(response.adoptionId()).isEqualTo(10L);
            assertThat(response.catId()).isEqualTo(1L);
            assertThat(response.catName()).isEqualTo("나비");
            assertThat(response.applicantId()).isEqualTo(2L);
            assertThat(response.applicantNickname()).isEqualTo("nickname");
            assertThat(response.message()).isEqualTo("사유");
            assertThat(response.status()).isEqualTo(AdoptionRequestStatus.REQUESTED);
            assertThat(response.createdAt()).isEqualTo(createdAt);
        }
    }

    @Nested
    @DisplayName("신청자 닉네임")
    class ApplicantNickname {

        @Test
        @DisplayName("신청자가 탈퇴하지 않았으면 닉네임이 그대로 나온다")
        void shows_nickname_when_active() {
            Cat cat = mock(Cat.class);
            Member member = new Member("login", "pw", "nickname");
            AdoptionRequest request = new AdoptionRequest(cat, member, "사유");

            AdoptionResponse response = AdoptionResponse.from(request);

            assertThat(response.applicantNickname()).isEqualTo("nickname");
        }

        @Test
        @DisplayName("신청자가 탈퇴했으면 닉네임 대신 \"탈퇴한 회원\"이 나온다")
        void shows_withdrawn_label_when_withdrawn() {
            Cat cat = mock(Cat.class);
            Member member = new Member("login", "pw", "nickname");
            member.withdraw();
            AdoptionRequest request = new AdoptionRequest(cat, member, "사유");

            AdoptionResponse response = AdoptionResponse.from(request);

            assertThat(response.applicantNickname()).isEqualTo("탈퇴한 회원");
        }
    }
}
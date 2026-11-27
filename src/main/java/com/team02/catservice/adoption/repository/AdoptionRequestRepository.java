package com.team02.catservice.adoption.repository;

import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AdoptionRequestRepository extends JpaRepository<AdoptionRequest,Long> {
    /** [1] 같은 고양이에 내 신청 중 주어진 상태(진행 중)의 신청이 있는지 확인한다. 중복 신청을 막는 데 쓴다 (26.11.24/안형빈) */
    boolean existsByCatIdAndApplicantIdAndStatusIn(
            Long catId,
            Long applicantId,
            Collection<AdoptionRequestStatus> statuses
    );

    /** [2] 내가 보낸 신청 목록을 페이징해서 조회한다 (26.11.24/안형빈) */
    @EntityGraph(attributePaths = {"cat", "applicant"})
    Page<AdoptionRequest> findByApplicantId(
            Long applicantId,
            Pageable pageable
    );

    /** [3] 같은 고양이의 특정 상태 신청 목록을 조회한다. 입양 완료나 공고 닫기 때 남은 REQUESTED 신청을 거절하는 데 쓴다 (26.11.24/안형빈) */
    List<AdoptionRequest> findByCatIdAndStatus(
            Long catId,
            AdoptionRequestStatus status
    );

    /** [4] 그 고양이에 주어진 상태(진행 중)의 신청이 하나라도 있는지 확인한다. 고양이 삭제를 막는 데 쓴다 (26.11.24/안형빈) */
    boolean existsByCatIdAndStatusIn(
            Long catId,
            Collection<AdoptionRequestStatus> statuses
    );

    /** [5] 내 고양이에 들어온 신청 목록을 페이징해서 조회한다. 보호자가 나인 고양이의 신청이다 (26.11.27/안형빈) */
    @EntityGraph(attributePaths = {"cat", "applicant"})
    Page<AdoptionRequest> findByCatMemberId(
            Long memberId,
            Pageable pageable
    );

    /** [6] 내가 신청한 적이 있는 고양이 번호를 모두 조회한다. 신청 상태와 무관하다. 추천에서 제외하는 데 쓴다 (26.11.27/안형빈) */
    @Query("""
            SELECT DISTINCT r.cat.id
            FROM AdoptionRequest r
            WHERE r.applicant.id = :applicantId
            """)
    List<Long> findCatIdsByApplicantId(
            @Param("applicantId")
            Long applicantId
    );
}
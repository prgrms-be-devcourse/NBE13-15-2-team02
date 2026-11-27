package com.team02.catservice.cat.repository;


import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.entity.CatAdoptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CatRepository extends JpaRepository<Cat, Long>, JpaSpecificationExecutor<Cat> {

    // GET /members/me/cats
    Page<Cat> findAllByMemberId(Long memberId, Pageable pageable);

    // 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Cat c where c.id = :catId")
    Optional<Cat> findByIdForUpdate(@Param("catId") Long catId);

    // 추천 후보: 입양 가능(status)하고 내가 등록하지 않은 고양이를 태그까지 한 번에 조회
    @Query("""
            select distinct c from Cat c
            left join fetch c.catTags ct
            left join fetch ct.tag
            where c.adoptionStatus = :status
            and c.member.id <> :memberId
            """)
    List<Cat> findAllWithTagsByStatusExcludingMember(@Param("status") CatAdoptionStatus staus,
                                                     @Param("memberId") Long memberId);
}

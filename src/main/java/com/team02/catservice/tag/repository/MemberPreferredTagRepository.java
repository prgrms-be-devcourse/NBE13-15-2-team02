package com.team02.catservice.tag.repository;

import com.team02.catservice.tag.entity.MemberPreferredTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemberPreferredTagRepository extends JpaRepository<MemberPreferredTag, Long> {

    // 회원의 선호 태그를 한 번에 조회한다.
    @Query("""
            select mpt from MemberPreferredTag mpt
            join fetch mpt.tag t
            where mpt.member.id = :memberId
            order by t.type, t.id
            """)
    List<MemberPreferredTag> findAllWithTagByMemberId(@Param("memberId") Long memberId);

    /**
     * 벌크 삭제를 하는 이유
     * Hibernate는 flush할 때 SQL을 INSERT -> UPDATE -> DELETE 순서로 보낸다.
     * deleteAll(기존 것) 후 saveAll(새 것)을 하면, INSERT -> DELETE 순으로 실행되어
     * 유니크 제약에 걸리기 때문에 벌크 삭제를 한다.
     */
    // 선호 태그 전체 교체(PUT) 때 기존 것을 지운다.
    @Modifying(flushAutomatically = true) // 실행 전에 아직 DB에 안 나간 변경을 먼저 flush함
    @Query("delete from MemberPreferredTag mpt where mpt.member.id = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);
}

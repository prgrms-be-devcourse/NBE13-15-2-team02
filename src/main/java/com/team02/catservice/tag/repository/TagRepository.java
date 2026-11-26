package com.team02.catservice.tag.repository;

import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.entity.TagType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    // 이름 여러 개를 IN 절로 한 번에 조회한다.
    List<Tag> findAllByNameIn(Collection<String> name);

    /**
     * GET /tags 의 type, keyword 필터.
     * <p>
     * (:type is null or t.type = :type) -> 파라미터가 null이면 앞쪽이 참이 되어 그 조건을 통째로 무시함.
     */
    @Query("""
            select t from Tag t
            where (:type is null or t.type = :type)
            and (:keyword is null or t.name like concat('%', :keyword, '%'))
            """)
    Page<Tag> search(@Param("type") TagType type,
                     @Param("keyword") String keyword,
                     Pageable pageable);
}

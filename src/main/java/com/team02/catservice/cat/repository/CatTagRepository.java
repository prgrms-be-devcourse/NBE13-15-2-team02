package com.team02.catservice.cat.repository;

import com.team02.catservice.cat.entity.CatTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

// CatTag 조회 최적화용
public interface CatTagRepository extends JpaRepository<CatTag, Long> {

    @Query("""
            select ct from CatTag ct
            join fetch ct.tag
            where ct.cat.id in :catIds
            order by ct.cat.id, ct.id
            """)
    List<CatTag> findAllWithTagByCatIdIn(@Param("catIds") Collection<Long> catIds);
}

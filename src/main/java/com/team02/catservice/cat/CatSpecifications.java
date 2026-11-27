package com.team02.catservice.cat;

import com.team02.catservice.cat.entity.*;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

// GET /cats 필터(데이터 정규화) 조건 모음 클래스
public final class CatSpecifications {

    private CatSpecifications() {
    }

    /**
     * - root : 조회 대상(Cat). root.get("type") = c.type
     * - query : 전체 쿼리. 서브쿼리를 만들 때 사용
     * - cb : CriteriaBuilder. equal, exists, and 같은 조건식을 만듦.
     */

    // Cat 테이블에서 type을 가져와서 cb.equal로 type 같은지 여부 확인
    public static Specification<Cat> typeEq(CatType type) {
        return (root, query, cb) -> cb.equal(root.get("type"), type);
    }

    public static Specification<Cat> adoptionStatusEq(CatAdoptionStatus status) {
        return (root, query, cb) -> cb.equal(root.get("adoptionStatus"), status);
    }

    public static Specification<Cat> breedEq(Breed breed) {
        return (root, query, cb) -> cb.equal(root.get("breed"), breed);
    }

    public static Specification<Cat> genderEq(Gender gender) {
        return (root, query, cb) -> cb.equal(root.get("gender"), gender);
    }

    /**
     * 특정 태그가 붙은 고양이만 필터.
     * <p>
     * WHERE EXISTS (
     *       SELECT ct.id FROM cat_tag ct JOIN tag t ON ct.tag_id = t.id
     *       WHERE ct.cat_id = c.id AND t.name = ?
     *   )
     */

    public static Specification<Cat> hasTag(String normalizedTagName) {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<CatTag> catTag = sub.from(CatTag.class);
            sub.select(catTag.get("id"))
                    .where(
                            cb.equal(catTag.get("cat"), root),
                            cb.equal(catTag.get("tag").get("name"), normalizedTagName)
                    );
            return cb.exists(sub);
        };
    }
}

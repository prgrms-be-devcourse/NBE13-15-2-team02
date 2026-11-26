package com.team02.catservice.cat.service;

import com.team02.catservice.cat.CatSpecifications;
import com.team02.catservice.cat.dto.*;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.entity.CatAdoptionStatus;
import com.team02.catservice.cat.entity.CatTag;
import com.team02.catservice.cat.event.CatAdoptionClosedEvent;
import com.team02.catservice.cat.event.CatDeletedEvent;
import com.team02.catservice.cat.event.CatDeletionRequestedEvent;
import com.team02.catservice.cat.repository.CatRepository;
import com.team02.catservice.cat.repository.CatTagRepository;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.service.TagService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.*;

/**
 * 고양이 CRUD, 필터 검색, 입양 공고 열기·닫기, adoption(팀원4)용 고양이 상태 변경 메서드
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatService {

    // 추정 출생연도는 올해로부터 이 기간 안이어야 함. ex) 2026 - 30 = 1996 -> 1996 ~ 2026
    private static final int MAX_CAT_AGE = 30;

    private final CatRepository catRepository;
    private final CatTagRepository catTagRepository; // 추천용 태그 일괄 조회 (3-1)
    private final MemberService memberService;
    private final TagService tagService;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager; // 락 획득 후 refresh용

    // - API -

    /**
     * POST /cats
     * <p>
     * [순서]
     * 1. Cat 생성
     * 2. 태그를 찾거나 만듦. 새 태그는 이 시점에서 INSERT 되어 id가 생김.
     * 3. cat.replaceTags()로 catTag를 컬렉션에 추가함.
     * 4. save(cat) -> Cat INSERT 후, cascade = ALL 에 따라 CatTag들도 INSERT 된다.
     * <p>
     * Cat에 id가 없어도 CatTag가 Cat 객체를 참조하고 있어 Hibernate가 Cat을 먼저 저장하고,
     * 생긴 id로 cat_tag.cat_id를 채운다.
     */
    @Transactional
    public CatResponse creatCat(Long memberId, CatCreateRequest request) {
        validateBirthYear(request.birthYear()); // 추정 출생연도 검증
        Member member = memberService.getMember(memberId);

        // 품종(breed), 성별(gender), 중성화 여부(neutered)가 null이면 엔티티 생성자가 UNKNOWN으로 채워줌.
        // 빌더 패턴 방식
        Cat cat = Cat.builder()
                .member(member)
                .name(request.name())
                .type(request.type())
                .breed(request.breed())
                .gender(request.gender())
                .birthYear(request.birthYear())
                .neutered(request.neutered())
                .description(request.description())
                .imageUrl(request.imageUrl())
                .build();

        List<Tag> tags = tagService.findOrCreateAll(request.tags());
        cat.replaceTags(tags); // 태그 전체 교체

        catRepository.save(cat);
        return CatResponse.from(cat);
    }

    /**
     * GET /cats/{catId}
     * getCat(Long)은 adoption용으로 엔티티를 돌려주므로, API용은 getCatDetail로 구분한다.
     */
    // 메서드명 기존 getCat -> getCatDetail로 변경
    public CatResponse getCatDetail(Long catId) {
        return CatResponse.from(getCat(catId));
    }

    /**
     * GET /cats
     * <p>
     * 값이 들어온 조건만 Specification으로 만들어 AND로 묶음.
     * 단, 조건이 하나도 없으면 allOf(빈 리스트)가 "조건 없음"이 되어 전체 고양이를 조회한다.
     */
    public Page<CatResponse> getCats(CatSearchCondition condition, Pageable pageable) {
        return catRepository.findAll(toSpecification(condition), pageable)
                .map(CatResponse::from);
    }

    /**
     * GET /members/me/cats
     */
    public Page<CatResponse> getMyCats(Long memberId, Pageable pageable) {
        return catRepository.findAllByMemberId(memberId, pageable)
                .map(CatResponse::from);
    }

    /**
     * PATCH /cats/{catId}
     * <p>
     * tags: null이면 유지, 빈 배열이면 모두 삭제, 값이 있으면 전체 교체한다.
     * 입양 상태는 여기서 바꿀 수 없음.
     */
    @Transactional
    public CatResponse updateCat(Long memberId, Long catId, CatUpdateRequest request) {
        validateBirthYear(request.birthYear());
        Cat cat = requireOwner(memberId, catRepository.findById(catId));

        cat.update(request.name(), request.type(), request.breed(), request.gender(),
                request.birthYear(), request.neutered(), request.description(), request.imageUrl());

        // tags != null -> 태그 전체 교체
        if (request.tags() != null) {
            cat.replaceTags(tagService.findOrCreateAll(request.tags()));
        }
        return CatResponse.from(cat);
    }

    /**
     * 공고 관련
     * PATCH /cats/{catId}/adoption-status
     * 공고 열기(NONE -> AVAILABLE), 공고 닫기(AVAILABLE -> NONE)
     * <p>
     * 상태 검증(이미 열려 있음, RESERVED라서 닫을 수 없음. 길고양이)은 엔티티 메서드가 409로 막음.
     */
    @Transactional
    public CatResponse changeAdoptionStatus(Long memberId, Long catId, CatAdoptionStatusRequest request) {
        Cat cat = requireOwner(memberId, catRepository.findByIdForUpdate(catId)); // 여기서 비관적 락 걸음

        switch (request.adoptionStatus()) {
            case AVAILABLE -> cat.openAdoption(); // 입양 가능 -> 공고 열기
            case NONE -> {
                // 입양 대상 아님 -> 공고 닫기
                cat.closeAdoption();
                // [1] 입양 공고를 닫을 때 발행. adoption이 받아서 남은 REQUESTED 신청을 모두 거절한다
                // [2-2] adoption이 받아서 남은 REQUESTED 신청을 모두 거절한다.
                eventPublisher.publishEvent(new CatAdoptionClosedEvent(cat.getId()));
            }
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return CatResponse.from(cat);
    }

    /**
     * DELETE /cats/{catId}
     * <p>
     * TODO: 끝난 신청(COMPLETED·REJECTED·CANCELED)도 adoption_request.cat_id로
     *       이 고양이를 참조하므로 그대로 지우면 FK 오류남. 이력 삭제 / soft delete 중 정해야 함.
     */
    @Transactional
    public void deleteCat(Long memberId, Long catId) {
        Cat cat = requireOwner(memberId, catRepository.findByIdForUpdate(catId));

        // [1] 고양이를 삭제하기 전에 발행. adoption이 받아서 진행 중인 신청이 있으면 예외로 막는다.
        eventPublisher.publishEvent(new CatDeletionRequestedEvent(cat.getId())); // [2-1] adoption

        // [1] 고양이를 삭제할 때 발행. post가 받아서 연결된 글의 cat을 null로 바꾼다.
        eventPublisher.publishEvent(new CatDeletedEvent(cat.getId())); // [2-3] post

        // CatTag는 cascade = ALL, orphanRemoval = true라서 고양이 삭제시 함께 삭제된다.
        catRepository.delete(cat);
    }


    /*
      - adoption(팀원4)용 공개 메서드 -

      규칙: 다른 패키지는 Cat의 상태를 바꿀 때, 엔티티 메서드를 직접 부르지 않고, 이 메서드들을 거칩니다.

      [상태 변경 규칙 검사]
      - cat 쪽(여기와 Cat 엔티티): 고양이 상태 값 자체의 전이 규칙.
        ex) AVAILABLE이어야 RESERVED로, RESERVED에서 공고 닫기 불가능, 길고양이는 공고 불가능.
        어긋나면 Cat 엔티티가 409 INVALID_ADOPTION_STATUS를 던짐.
      - adoption 쪽: 신청에 관한 규칙.
        ex) 신청 가능 여부(AVAILABLE인지, 본인 고양이가 아닌지), 같은 고양이에 내 진행 중 신청이 없는지,
        신청 상태 전이(REQUESTED -> APPROVED 등), 행위자 권한(보호자·신청자).

      [트랜잭션 전파]
      아래 메서드의 @Transactional은 기본 전파(REQUIRED)라서, adoption 서비스의 트랜잭션 안에서
      호출되면 새 트랜잭션을 만들지 않고 그 트랜잭션에 합류한다.
      따라서 승인 처리 도중 adoption 쪽에서 예외가 나면 고양이 상태 변경도 함께 롤백된다.

      [호출 순서]
      adoption은 getCat()으로 보호자를 확인한 뒤에 markReserved 등을 호출해도 된다.
      getCatForUpdate()가 비관적 락을 건 뒤, refresh로 DB의 최신 값을 다시 읽어 검사하기 때문이다.
     */

    /**
     * [1-1] 고양이 조회. 없으면 CAT_NOT_FOUND(404).
     * <p>
     * 반환 타입은 엔티티(Cat). adoption에서 다음 용도로 쓴다.
     * - 신청 시: AVAILABLE 여부(cat.getAdoptionStatus())와 본인 고양이 여부 확인. AdoptionRequest.cat 설정
     * - 승인·거절·완료 시: 보호자 확인
     * <p>
     * 보호자 ID는 cat.getMember().getId()로 읽는다.
     * member는 LAZY 프록시지만 id는 프록시가 이미 갖고 있어서 getId()만으로는 member 조회가 일어나지 않는다.
     */
    public Cat getCat(Long catId) {
        return catRepository.findById(catId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND));
    }

    /**
     * [1-2] 입양 신청 승인: AVAILABLE -> RESERVED
     * 이미 RESERVED면 409 (고양이 하나에 APPROVED는 1건)
     */
    @Transactional
    public void markReserved(Long catId) {
        getCatForUpdate(catId).reserve();
    }

    /**
     * [1-3] 승인된 신청의 거절·취소: RESERVED -> AVAILABLE
     */
    @Transactional
    public void markAvailable(Long catId) {
        getCatForUpdate(catId).release();
    }

    /**
     * [1-4] 입양 완료: RESERVED -> ADOPTED
     */
    @Transactional
    public void markAdopted(Long catId) {
        getCatForUpdate(catId).completeAdoption();
    }

    /**
     * 추천 후보: AVAILABLE이면서 내가 등록하지 않은 고양이를 태그까지 채워서 조회한다.
     * "이미 신청한 고양이 제외"는 신청 데이터를 가진 adoption 쪽에서 거른다.
     */
    public List<Cat> getRecommendationCandidates(Long memberId) {
        return catRepository.findAllWithTagsByStatusExcludingMember(CatAdoptionStatus.AVAILABLE, memberId);
    }

    /**
     * [3-1] 여러 고양이의 태그를 한 번에 조회한다. (추천 점수 계산용: 성격 3점, 외모 2점)
     * <p>
     * 반환: catId -> 그 고양이의 태그 목록 (TagResponse에 이름과 타입이 들어 있음)
     * <p>
     * - IN 쿼리 한 번으로 가져오므로 후보가 많아도 쿼리는 1번이다 (고양이마다 조회하면 N번).
     * - 요청한 catId는 태그가 없어도 빈 리스트로 Map에 넣어 둔다.
     *   그래서 adoption은 map.get(catId)가 null인지 따로 확인할 필요가 없다.
     * - Map 순서는 요청한 catIds 순서를 따른다 (LinkedHashMap).
     * - ct.getCat().getId()는 cat 프록시가 가진 id라서 cat 테이블을 다시 조회하지 않는다.
     */
     public Map<Long, List<TagResponse>> getTagsByCatIds(Collection<Long> catIds) {
         Map<Long, List<TagResponse>> result = new LinkedHashMap<>();

         if (catIds == null || catIds.isEmpty()) {
             return result;
         }

         catIds.forEach(catId -> result.put(catId, new ArrayList<>()));

         for (CatTag catTag : catTagRepository.findAllWithTagByCatIdIn(catIds)) {
             result.get(catTag.getCat().getId()).add(TagResponse.from(catTag.getTag()));
         }

         return result;
     }

    // - 내부 메서드 -

    /** 락을 걸고, 최신 상태로 다시 읽은 고양이 (DB refresh) */
    private Cat getCatForUpdate(Long catId) {
        Cat cat = catRepository.findByIdForUpdate(catId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND));
        entityManager.refresh(cat);
        return cat;
    }

    /**
     * cat 존재 확인(404) -> cat 소유자(본인) 확인(403)
     */
    private Cat requireOwner(Long memberId, Optional<Cat> found) {
        Cat cat = found.orElseThrow(() -> new BusinessException(ErrorCode.CAT_NOT_FOUND));
        if (!cat.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return cat;
    }

    /**
     * 추정 출생연도 검증 로직. null은 '모름'이므로 return;
     */
    private void validateBirthYear(Integer birthYear) {
        if (birthYear == null) {
            return;
        }
        int thisYear = Year.now().getValue(); // 올해

        // 출생연도 > 올해(2026) || 출생연도 < 2026 - 30 = 1996
        if (birthYear > thisYear || birthYear < thisYear - MAX_CAT_AGE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    /**
     * 검색 조건 -> Specification
     * 태그는 저장된 이름과 비교해야 하므로 저장 규칙과 같이 정규화함. ex) "#개냥이" -> "개냥이"
     * 고양이의 유형, 입양 상태, 품종, 성별, 태그 값이 같은지 검증함.
     */
    private Specification<Cat> toSpecification(CatSearchCondition condition) {
        List<Specification<Cat>> specs = new ArrayList<>();

        if (condition.type() != null) {
            specs.add(CatSpecifications.typeEq(condition.type()));
        }

        if (condition.adoptionStatus() != null) {
            specs.add(CatSpecifications.adoptionStatusEq(condition.adoptionStatus()));
        }

        if (condition.breed() != null) {
            specs.add(CatSpecifications.breedEq(condition.breed()));
        }

        if (condition.gender() != null) {
            specs.add(CatSpecifications.genderEq(condition.gender()));
        }

        if (condition.tag() != null && !condition.tag().isBlank()) {
            specs.add(CatSpecifications.hasTag(tagService.normalize(condition.tag())));
        }

        return Specification.allOf(specs);
    }
}

package com.team02.catservice.cat.entity;

import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.*;

@Entity
@Table(name = "cat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cat extends BaseEntity {

    public static final int MAX_TAG_COUNT = 10;

    // 고양이 등록자 및 보호자 N:1
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 30)
    private String name;

    // 반려묘, 스트릿, 임보
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatType type;

    // 고양이 품종
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Breed breed;

    // 고양이 성별
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    // 출생연도, 모르면 null
    private Integer birthYear;

    // 중성화 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Neutered neutered;

    // 입양 여부
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatAdoptionStatus adoptionStatus;

    // 추가 설명
    @Column(length = 500)
    private String description;

    // 이미지
    private String imageUrl;

    // 고양이 태그들
    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CatTag> catTags = new ArrayList<>();

    @Builder // 빌더 패턴 방식
    private Cat(Member member, String name, CatType type, Breed breed, Gender gender, Integer birthYear,
                Neutered neutered, String description, String imageUrl) {
        this.member = member;
        this.name = name;
        this.type = type;
        this.breed = breed != null ? breed : Breed.UNKNOWN;
        this.gender = gender != null ? gender : Gender.UNKNOWN;
        this.birthYear = birthYear;
        this.neutered = neutered != null ? neutered : Neutered.UNKNOWN;
        this.adoptionStatus = CatAdoptionStatus.NONE;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // - 조회 편의 메서드 -

    // 소유자인지
    public boolean isOwnedBy(Long memberId) {
        return member.getId().equals(memberId);
    }

    // 나이
    public Integer getAge() {
        if (birthYear == null) {
            return null;
        }
        return Math.max(0, Year.now().getValue() - birthYear);
    }

    // 고양이 태그들 매핑
    public List<Tag> getTags() {
        return catTags.stream().map(CatTag::getTag).toList();
    }

    // - 정보 수정 메서드 -
    // 단, null일 경우에는 값 변경x

    public void update(String name, CatType type, Breed breed, Gender gender, Integer birthYear, Neutered neutered,
                       String description, String imageUrl) {
        if (name != null) this.name = name;
        if (type != null) changeType(type);
        if (breed != null) this.breed = breed;
        if (gender != null) this.gender = gender;
        if (birthYear != null) this.birthYear = birthYear;
        if (neutered != null) this.neutered = neutered;
        if (description != null) this.description = description;
        if (imageUrl != null) this.imageUrl = imageUrl;
    }

    // 입양 공고가 열린 고양이 -> 길고양이로 변경시 규칙이 깨짐
    private void changeType(CatType newType) {
        // 길고양이 && 입양 대상 아닐시
        if (newType == CatType.STRAY && adoptionStatus != CatAdoptionStatus.NONE) {
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS);
        }
        this.type = newType;
    }

    // - 태그 관련 메서드 -

    // 태그 전체 교체
    public void replaceTags(Collection<Tag> tags) {
        Map<Long, Tag> newTags = new LinkedHashMap<>();

        for (Tag tag : tags) {
            // putIfAbsent() - 맵에 키가 없거나 값이 null 일 때만 새 값 저장, 이미 값이 있으면 기존 값 유지함.
            newTags.putIfAbsent(tag.getId(), tag);
        }

        // 태그가 10개 이상일 경우
        if (newTags.size() > MAX_TAG_COUNT) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        // removeIf() - 만족하는 요소를 한 번에 제거
        catTags.removeIf(catTag -> !newTags.containsKey(catTag.getTag().getId()));

        Set<Long> currentTagIds = new HashSet<>();
        catTags.forEach(catTag -> currentTagIds.add(catTag.getTag().getId()));

        newTags.forEach((tagId, tag) -> {
            if (currentTagIds.add(tagId)) {
                catTags.add(CatTag.of(this, tag));
            }
        });
    }

    // - 입양 상태 전이 -

    // 공고 열기(보호자): NONE(입양 대상 아님) -> AVAILABLE(입양 가능). 길고양이는 불가능
    public void openAdoption() {
        if (type == CatType.STRAY) {
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS);
        }
        requireStatus(CatAdoptionStatus.NONE);
        this.adoptionStatus = CatAdoptionStatus.AVAILABLE;
    }

    // 공고 닫기(보호자): AVAILABLE(입양 가능) -> NONE(입양 대상 아님). RESERVED(입양 진행 중)면 불가능
    public void closeAdoption() {
        requireStatus(CatAdoptionStatus.AVAILABLE);
        this.adoptionStatus = CatAdoptionStatus.NONE;
    }

    // 입양 신청 승인: AVAILABLE(입양 가능) -> RESERVED(입양 진행 중)
    public void reserve() {
        requireStatus(CatAdoptionStatus.AVAILABLE);
        this.adoptionStatus = CatAdoptionStatus.RESERVED;
    }

    // 입양 신청 승인 철회·취소: RESERVED(입양 진행 중) -> AVAILABLE(입양 가능)
    public void release() {
        requireStatus(CatAdoptionStatus.RESERVED);
        this.adoptionStatus = CatAdoptionStatus.AVAILABLE;
    }

    // 입양 완료: RESERVED(입양 진행 중) -> ADOPTED(입양 완료)
    public void completeAdoption() {
        requireStatus(CatAdoptionStatus.RESERVED);
        this.adoptionStatus = CatAdoptionStatus.ADOPTED;
    }

    // 입양 여부 확인하는 메서드
    public void requireStatus(CatAdoptionStatus expected) {
        if (this.adoptionStatus != expected) {
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

}

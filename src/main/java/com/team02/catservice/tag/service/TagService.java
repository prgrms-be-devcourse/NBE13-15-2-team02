package com.team02.catservice.tag.service;

import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.entity.TagType;
import com.team02.catservice.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 태그 정규화·조회·생성
 * cat(고양이 태그), post(게시글 태그) 함께 쓰는 공용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    // 글, 고양이 최대 태그 수 10개
    public static final int MAX_TAGS_PER_TARGET = 10;

    /**
     * 공백 문자 패턴
     * \s 는 ASCII 공백(스페이스, 탭, 줄바꿈)만 잡는다.
     * \p{Z} 는 유니코드 "구분자" 분류로, 한글 입력기의 전각 공백과
     * 줄바꿈 없는 공백까지 잡는다. 둘을 합쳐 모든 공백을 지운다.
     */
    private static final String WHITESPACE = "[\\s\\p{Z}]+";

    /** 문자열 맨 앞(^)의 # 하나 이상(#+). ex) "##치즈" -> "치즈" */
    private static final String LEADING_HASH = "^#+";

    private final TagRepository tagRepository;

    /** 태그 이름 정규화 ex) "#낡씨 맑음" -> "날씨맑음", "#Cute Cat" -> "cutecat" */
    public String normalize(String raw) {
        if (raw == null) {
            throw new BusinessException(ErrorCode.INVALID_TAG_NAME);
        }

        String name = raw.replaceAll(WHITESPACE, "") // 공백 지움
                .replaceFirst(LEADING_HASH, "") // #지움
                .toLowerCase(Locale.ROOT); // 소문자 변환

        // 위의 정규화 후의 name 길이
        if (name.isEmpty() || name.length() > Tag.MAX_NAME_LENGTH) {
            throw new BusinessException(ErrorCode.INVALID_TAG_NAME);
        }

        return name;
    }

    /**
     * 이름 목록 -> 태그 엔티티 목록. 있으면 재사용, 없으면 FREE 태그로 만든다.
     * 고양이 등록·수정(CatService)과 게시글 작성·수정(PostService)에서 호출한다.
     * <p>
     * 처리 순서:
     * 1. 정규화 후 중복 제거
     *    ex) ["치즈", "#치즈", "개냥이"] -> ["치즈", "개냥이"]
     * 2. 태그 개수 검사
     * 3. IN 쿼리로 기존 태그 조회 Map<이름, Tag>
     * 4. Map에 없는 이름만 저장
     */
    @Transactional
    public List<Tag> findOrCreateAll(List<String> rawNames) {
        if (rawNames == null || rawNames.isEmpty()) {
            return List.of();
        }

        List<String> names = rawNames.stream()
                .map(this::normalize) // 정규화
                .distinct() // 중복 제거
                .toList();

        // 태그 > 10
        if (names.size() > MAX_TAGS_PER_TARGET) {
            throw new BusinessException(ErrorCode.TAG_LIMIT_EXCEEDED);
        }

        // tagRepository에서 존재하는 태그들 가져옴. (재사용)
        Map<String, Tag> existing = tagRepository.findAllByNameIn(names).stream()
                .collect(Collectors.toMap(Tag::getName, Function.identity()));

        List<Tag> result = new ArrayList<>(names.size());
        for (String name : names) {
            Tag tag = existing.get(name); // 태그 존재 여부
            // 존재하지 않으면 Free 태그로 새로 만듦.
            if (tag == null) {
                tag = tagRepository.save(Tag.createFree(name));
            }
            result.add(tag);
        }
        return result;
    }

    /**
     * id 목록으로 태그 조회. 없으면, TAG_NOT_FOUND(404)
     */
    public List<Tag> getTagsByIds(Collection<Long> tagIds) {
        Set<Long> ids = new LinkedHashSet<>(tagIds);
        List<Tag> tags = tagRepository.findAllById(ids);

        // 요청 개수와 결과 개수 비교
        if (tags.size() != ids.size()) {
            throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
        }
        return tags;
    }

    /** GET /tags */
    public Page<TagResponse> getTags(TagType type, String keyword, Pageable pageable) {
        return tagRepository.search(type, toSearchKeyword(keyword), pageable)
                .map(TagResponse::from);
    }

    /**
     * 검색어 다듬기.
     * ex) "#치"로 검색 -> "치"로 바꿔 "치즈"를 찾을 수 있게 함.
     * 빈 검색어의 경우에는 null로 바꿔 "필터 없음"으로 처리.
     */
    private String toSearchKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.replaceAll(WHITESPACE, "") // 공백 지움
                .replaceFirst(LEADING_HASH, "") // # 지움
                .toLowerCase(Locale.ROOT); // 소문자 변환
        return normalized.isEmpty() ? null : normalized;
    }

}

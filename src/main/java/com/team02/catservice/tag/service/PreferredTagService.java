package com.team02.catservice.tag.service;

import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import com.team02.catservice.tag.dto.PreferredTagRequest;
import com.team02.catservice.tag.dto.TagResponse;
import com.team02.catservice.tag.entity.MemberPreferredTag;
import com.team02.catservice.tag.entity.Tag;
import com.team02.catservice.tag.repository.MemberPreferredTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원의 선호 태그.
 * 추천(adoption)의 입력값이 된다.
 * <p>
 * Member는 MemberService, Tag는 TagService를 거쳐 가져온다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreferredTagService {

    private final MemberPreferredTagRepository preferredTagRepository;
    private final TagService tagService;

    // getMember(id): 없으면 MEMBER_NOT_FOUND(Member)
    private final MemberService memberService;

    /** GET /members/me/preferred-tags */
    public List<TagResponse> getMyPreferredTags(Long memberId) {
        return getPreferredTags(memberId);
    }

    /**
     * PUT /members/me/preferred-tags 기존 선호 태그를 모두 지우고, 요청한 것으로 바꾼다.
     * <p>
     * [검증 -> 삭제 -> 저장 순서]
     * 1. 태그 id가 모두 존재하는지 확인 (없으면 404)
     * 2. MemberPreferredTag.of()로 엔티티를 미리 만들며 프리셋인지 확인 (FREE면 400)
     * 3. 기존 것 벌크 삭제
     * 4. 새 것 저장
     */
    @Transactional
    public List<TagResponse> replacePreferredTags(Long memberId, PreferredTagRequest request) {
        Member member = memberService.getMember(memberId);
        List<Tag> tags = tagService.getTagsByIds(request.tagIds());

        // 새로운 태그들
        List<MemberPreferredTag> newPreferredTags = tags.stream()
                .map(tag -> MemberPreferredTag.of(member, tag))
                .toList();

        preferredTagRepository.deleteAllByMemberId(memberId); // 기존 태그들 벌크 삭제
        preferredTagRepository.saveAll(newPreferredTags); // 새로운 태그들 저장

        return getMyPreferredTags(memberId);
    }


    // - adoption용 메서드 -

    /**
     * [3-2] adoption 추천용 태그 조회 (내 선호 태그 조회)
     * TagResponse에 이름과 타입(APPEARANCE, PERSONALITY, FREE)이 들어 있다.
     * <p>
     * 회원의 선호 태그 엔티티 목록. 추천 점수 계산에서 사용한다.
     * Tag.getType()으로 PERSONALITY(3점)와 APPEARANCE(2점)를 나눌 수 있다.
     * 만약 비어 있으면 "선호 태그 없음"이므로 추천은 최근 등록순으로 대체한다.
     */
    public List<TagResponse> getPreferredTags(Long memberId) {
        return preferredTagRepository.findAllWithTagByMemberId(memberId).stream()
                .map(memberPreferredTag ->
                        TagResponse.from(memberPreferredTag.getTag()))
                .toList();
    }
}

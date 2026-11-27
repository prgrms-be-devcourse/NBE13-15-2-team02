package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.AdoptionCreateRequest;
import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.cat.entity.CatAdoptionStatus;
import com.team02.catservice.cat.service.CatService;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import com.team02.catservice.global.response.PageResponse;
import com.team02.catservice.member.entity.Member;
import com.team02.catservice.member.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdoptionService {
    private final AdoptionRequestRepository adoptionRequestRepository;
    private final CatService catService;
    private final MemberService memberService;

    @Autowired
    public AdoptionService(AdoptionRequestRepository adoptionRequestRepository, CatService catService, MemberService memberService) {
        this.adoptionRequestRepository = adoptionRequestRepository;
        this.catService = catService;
        this.memberService = memberService;
    }

    /** [1] 내가 보낸 입양 신청 목록을 페이징해서 조회한다 (26.11.25/안형빈) */
    public PageResponse<AdoptionResponse> getMyAdoptions(Long memberId, Pageable pageable){
        /* [1-1] 신청자가 나인 신청을 조회하고, 신청 한 건씩 응답 DTO로 바꾼다. */
        Page<AdoptionResponse> page =  adoptionRequestRepository
                .findByApplicantId(memberId,pageable)
                .map(AdoptionResponse::from);

        /* [1-2] 목록과 페이지 정보(page, size, totalElements, totalPages)를 담아 돌려준다. */
        return PageResponse.from(page);
    }

    /** [2] 입양을 신청한다. 고양이와 신청 조건을 확인한 뒤 REQUESTED 상태의 신청을 새로 만든다 (26.11.26/안형빈) */
    @Transactional
    public AdoptionResponse createAdoption(Long memberId, AdoptionCreateRequest request){
        /* [2-1] 고양이를 조회한다. 없으면 404 */
        Cat cat = catService.getCat(request.catId());

        /* [2-2] 본인이 등록한 고양이에는 신청할 수 없다. (403) */
        if (cat.isOwnedBy(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인이 등록한 고양이에는 신청할 수 없습니다.");
        }

        /* [2-3] 입양 가능한 고양이에만 신청할 수 있다. (409) */
        if (cat.getAdoptionStatus() != CatAdoptionStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS, "입양 신청을 받지 않는 고양이입니다.");
        }

        /* [2-4] 같은 고양이에 내 진행 중 신청이 이미 있으면 중복 신청이다. (409) */
        if (adoptionRequestRepository.existsByCatIdAndApplicantIdAndStatusIn(
                cat.getId(), memberId, AdoptionRequestStatus.getInProgressStatuses())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ADOPTION_REQUEST);
        }

        /* [2-5] 신청자를 조회해 신청을 만들고 저장한다. (처음 상태는 REQUESTED) */
        Member applicant = memberService.getMember(memberId);
        AdoptionRequest adoptionRequest = adoptionRequestRepository.save(
                new AdoptionRequest(cat, applicant, request.message()));

        /* [2-6] 저장된 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(adoptionRequest);
    }
}
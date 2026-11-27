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

import java.util.List;

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

    /** [3] 입양 신청을 승인한다. 신청은 APPROVED, 고양이는 RESERVED로 함께 바꾼다 (26.11.27/안형빈) */
    @Transactional
    public AdoptionResponse approveAdoption(Long memberId, Long adoptionId){
        /* [3-1] 신청을 조회한다. 없으면 404 */
        AdoptionRequest request = getAdoptionRequest(adoptionId);

        /* [3-2] 그 고양이의 보호자만 승인할 수 있다. (403) */
        Cat cat = request.getCat();
        if(!cat.isOwnedBy(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 고양이의 보호자만 처리할 수 있습니다.");
        }

        /* [3-3] 신청을 승인하고 고양이를 예약 상태로 바꾼다. (상태가 맞지 않으면 409) */
        request.approve();
        catService.markReserved(cat.getId());

        /* [3-4] 처리된 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(request);
    }

    /** [4] 입양 신청을 거절한다. 승인된 신청을 거절(승인 철회)하면 고양이를 다시 입양 가능 상태로 돌린다 (26.11.27/안형빈) */
    @Transactional
    public AdoptionResponse rejectAdoption(Long memberId, Long adoptionId){
        /* [4-1] 신청을 조회한다. 없으면 404 */
        AdoptionRequest request = getAdoptionRequest(adoptionId);

        /* [4-2] 그 고양이의 보호자만 거절할 수 있다. (403) */
        Cat cat = request.getCat();
        if(!cat.isOwnedBy(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 고양이의 보호자만 처리할 수 있습니다.");
        }

        /* [4-3] 거절하면 상태가 바뀌므로, 먼저 승인된 신청이었는지 저장해 둔다. */
        AdoptionRequestStatus statusBefore = request.getStatus();

        /* [4-4] 신청을 거절한다. (상태가 맞지 않으면 409) */
        request.reject();

        /* [4-5] 승인됐던 신청이면 고양이를 다시 입양 가능 상태로 바꾼다. (REQUESTED였다면 고양이는 그대로) */
        if(statusBefore == AdoptionRequestStatus.APPROVED){
            catService.markAvailable(cat.getId());
        }

        /* [4-6] 처리된 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(request);
    }

    /** [5] 입양을 완료 처리한다. 고양이는 ADOPTED가 되고, 같은 고양이의 남은 신청은 모두 거절한다 (26.11.27/안형빈) */
    @Transactional
    public AdoptionResponse completeAdoption(Long memberId, Long adoptionId){
        /* [5-1] 신청을 조회한다. 없으면 404 */
        AdoptionRequest request = getAdoptionRequest(adoptionId);

        /* [5-2] 그 고양이의 보호자만 완료 처리할 수 있다. (403) */
        Cat cat = request.getCat();
        if(!cat.isOwnedBy(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 고양이의 보호자만 처리할 수 있습니다.");
        }

        /* [5-3] 신청을 완료 처리하고, 고양이를 입양 완료 상태로 바꾼다. (승인된 신청이 아니면 409) */
        request.complete();
        catService.markAdopted(cat.getId());
        /* [5-4] 같은 고양이의 남은 REQUESTED 신청을 모두 거절한다. */
        List<AdoptionRequest> waiting = adoptionRequestRepository
                .findByCatIdAndStatus(cat.getId(), AdoptionRequestStatus.REQUESTED);
        for(AdoptionRequest other : waiting){
            other.reject();
        }

        /* [5-5] 처리된 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(request);
    }

    /** [6] 입양 신청을 취소한다. 승인된 신청을 취소하면 고양이를 다시 입양 가능 상태로 돌린다 (26.11.27/안형빈) */
    @Transactional
    public AdoptionResponse cancelAdoption(Long memberId, Long adoptionId){
        /* [6-1] 신청을 조회한다. 없으면 404 */
        AdoptionRequest request = getAdoptionRequest(adoptionId);

        /* [6-2] 신청한 본인만 취소할 수 있다. (403) */
        if(!request.getApplicant().getId().equals(memberId)){
            throw new BusinessException(ErrorCode.FORBIDDEN, "신청한 본인만 취소할 수 있습니다.");
        }

        /* [6-3] 취소하면 상태가 바뀌므로, 먼저 승인된 신청이었는지 저장해 둔다. */
        AdoptionRequestStatus statusBefore = request.getStatus();

        /* [6-4] 신청을 취소한다. (입양 완료 뒤처럼 상태가 맞지 않으면 409) */
        request.cancel();

        /* [6-5] 승인됐던 신청이면 고양이를 다시 입양 가능 상태로 바꾼다. (REQUESTED였다면 고양이는 그대로) */
        if(statusBefore == AdoptionRequestStatus.APPROVED){
            catService.markAvailable(request.getCat().getId());
        }

        /* [6-6] 처리된 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(request);
    }

    /** [7] 입양 신청 상세를 조회한다. 신청자와 그 고양이의 보호자만 볼 수 있다 (26.11.27/안형빈) */
    public AdoptionResponse getAdoption(Long memberId, Long adoptionId){
        /* [7-1] 신청을 조회한다. 없으면 404 */
        AdoptionRequest request = getAdoptionRequest(adoptionId);

        /* [7-2] 신청자도 보호자도 아니면 볼 수 없다. (403) */
        boolean isApplicant = request.getApplicant().getId().equals(memberId);
        boolean isOwner = request.getCat().isOwnedBy(memberId);
        if (!isApplicant && !isOwner) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "신청자와 보호자만 조회할 수 있습니다.");
        }

        /* [7-3] 신청을 응답으로 바꿔 돌려준다. */
        return AdoptionResponse.from(request);
    }

    /** [8] 내 고양이에 들어온 입양 신청 목록을 페이징해서 조회한다 (26.11.27/안형빈) */
    public PageResponse<AdoptionResponse> getReceivedAdoptions(Long memberId, Pageable pageable){
        /* [8-1] 보호자가 나인 고양이의 신청을 조회하고, 신청 한 건씩 응답 DTO로 바꾼다. */
        Page<AdoptionResponse> page = adoptionRequestRepository
                .findByCatMemberId(memberId, pageable)
                .map(AdoptionResponse::from);

        /* [8-2] 목록과 페이지 정보(page, size, totalElements, totalPages)를 담아 돌려준다. */
        return PageResponse.from(page);
    }

    /** [9] 신청 번호로 신청을 조회한다. 없으면 404 ADOPTION_NOT_FOUND (26.11.27/안형빈) */
    private AdoptionRequest getAdoptionRequest(Long adoptionId){
        return adoptionRequestRepository.findById(adoptionId)
                .orElseThrow(()-> new BusinessException(ErrorCode.ADOPTION_NOT_FOUND));
    }
}
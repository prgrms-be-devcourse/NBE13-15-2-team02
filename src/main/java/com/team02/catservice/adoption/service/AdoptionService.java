package com.team02.catservice.adoption.service;

import com.team02.catservice.adoption.dto.AdoptionResponse;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.global.response.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdoptionService {
    AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    public AdoptionService(AdoptionRequestRepository adoptionRequestRepository) {
        this.adoptionRequestRepository = adoptionRequestRepository;
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
}
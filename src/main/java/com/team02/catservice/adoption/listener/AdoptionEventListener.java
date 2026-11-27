package com.team02.catservice.adoption.listener;

import com.team02.catservice.adoption.entity.AdoptionRequest;
import com.team02.catservice.adoption.entity.AdoptionRequestStatus;
import com.team02.catservice.adoption.repository.AdoptionRequestRepository;
import com.team02.catservice.cat.event.CatAdoptionClosedEvent;
import com.team02.catservice.cat.event.CatDeletionRequestedEvent;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(readOnly = true)
public class AdoptionEventListener {
    private final AdoptionRequestRepository adoptionRequestRepository;

    @Autowired
    public AdoptionEventListener(AdoptionRequestRepository adoptionRequestRepository) {
        this.adoptionRequestRepository = adoptionRequestRepository;
    }

    @EventListener
    public void handleCatDeletionRequested(CatDeletionRequestedEvent event){
        Long catId = event.catId();
        if(adoptionRequestRepository.existsByCatIdAndStatusIn(catId, AdoptionRequestStatus.getInProgressStatuses())){
            throw new BusinessException(ErrorCode.INVALID_ADOPTION_STATUS);
        }
    }

    @EventListener
    @Transactional
    public void handleCatAdoptionClosed(CatAdoptionClosedEvent event){
        Long catId = event.catId();
        List<AdoptionRequest> adoptionRequestList = adoptionRequestRepository.findByCatIdAndStatus(catId, AdoptionRequestStatus.REQUESTED);

        for(AdoptionRequest adoptionRequest : adoptionRequestList){
            adoptionRequest.reject();
        }
    }
}
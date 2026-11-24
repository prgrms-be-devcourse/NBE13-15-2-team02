package com.team02.catservice.adoption.entity;

import com.team02.catservice.cat.entity.Cat;
import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdoptionRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cat_id")
    private Cat cat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_id")
    private Member applicant;

    @Column(name = "message", length = 500, nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AdoptionRequestStatus status;

    public AdoptionRequest(Cat cat, Member applicant, String message) {
        this.cat = cat;
        this.applicant = applicant;
        this.message = message;
        this.status = AdoptionRequestStatus.REQUESTED;
    }

    public void approve() {
        this.status = this.status.approve();
    }

    public void reject() {
        this.status = this.status.reject();
    }

    public void complete() {
        this.status = this.status.complete();
    }

    public void cancel() {
        this.status = this.status.cancel();
    }
}
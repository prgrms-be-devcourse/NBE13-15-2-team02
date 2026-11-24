package com.team02.catservice.tag.entity;

import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.global.exception.BusinessException;
import com.team02.catservice.global.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
/*
@Table(
        name = "member_preferred_tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_preferred_tag",
                columnNames = {"member_id", "tag_id"}
        )
)
*/
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberPreferredTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

//    private MemberPreferredTag(Member member, Tag tag) {
//        this.member = member;
//        this.tag = tag;
//    }

   /* public static MemberPreferredTag of(Member member, Tag tag) {
        if (!tag.isPreset()) {
            throw new BusinessException(ErrorCode.INVALID_TAG);
        }
        return new MemberPreferredTag(member, tag);
    }*/
}

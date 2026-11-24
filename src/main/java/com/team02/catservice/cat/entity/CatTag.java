package com.team02.catservice.cat.entity;

import com.team02.catservice.global.entity.BaseEntity;
import com.team02.catservice.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
/*
@Table(
        name = "cat_tag",
        uniqueConstraints =  @UniqueConstraint(
                name = "uk_cat_tag",
                columnNames = {"cat_id", "tag_id"}
        )
)
*/
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "cat_id", nullable = false)
    private Cat cat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinCTagmn(name = "tag_id", nullable = false)
    private Tag tag;

    private CatTag(Cat cat, Tag tag) {
        this.cat = cat;
        this.tag = tag;
    }

    static CatTag of(Cat cat, Tag tag) {
        return new CatTag(cat, tag);
    }
}

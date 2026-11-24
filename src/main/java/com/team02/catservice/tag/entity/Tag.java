package com.team02.catservice.tag.entity;

import com.team02.catservice.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
//@Table(name = "tag")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity{

    public static final int MAX_NAME_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 정규화된 이름만 저장
    @Column(nullable = false, unique = true, length = MAX_NAME_LENGTH)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagType type;

    private Tag(String name, TagType type) {
        this.name = name;
        this.type = type;
    }

    // 프리셋에 없는 이름으로 새 태그를 만들 때 사용한다.
    public static Tag createFree(String normalizedName) {
        return new Tag(normalizedName, TagType.FREE);
    }

    public boolean isPreset() {
        return type.isPreset();
    }
}
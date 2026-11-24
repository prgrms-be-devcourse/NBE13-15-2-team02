package com.team02.catservice.tag.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagType {
    APPEARANCE("외모", true),
    PERSONALITY("성격", true),
    FREE("자유", false);

    private final String description;
    private final boolean preset;

}

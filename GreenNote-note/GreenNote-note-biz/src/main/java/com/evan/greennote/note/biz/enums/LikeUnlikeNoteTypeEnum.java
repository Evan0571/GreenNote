package com.evan.greennote.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LikeUnlikeNoteTypeEnum {
    LIKE(1),
    UNLIKE(0),
    ;

    private final Integer code;
}

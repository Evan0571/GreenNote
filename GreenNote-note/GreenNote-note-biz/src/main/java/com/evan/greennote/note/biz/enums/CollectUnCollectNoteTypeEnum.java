package com.evan.greennote.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CollectUnCollectNoteTypeEnum {
    COLLECT(1),
    UN_COLLECT(0),
    ;
    private final Integer code;
}

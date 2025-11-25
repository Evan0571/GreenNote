package com.evan.greennote.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoteOperateEnum {
    PUBLISH(1),
    DELETE(0),
    ;
    private final Integer code;
}

package com.evan.greennote.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum CollectUnCollectNoteTypeEnum {
    COLLECT(1),
    UN_COLLECT(0),
    ;
    private final Integer code;

    public static CollectUnCollectNoteTypeEnum valueOf(Integer code) {
        for (CollectUnCollectNoteTypeEnum collectUnCollectNoteTypeEnum : CollectUnCollectNoteTypeEnum.values()) {
            if(Objects.equals(code, collectUnCollectNoteTypeEnum.getCode())){
                return collectUnCollectNoteTypeEnum;
            }
        }
        return null;
    }
}

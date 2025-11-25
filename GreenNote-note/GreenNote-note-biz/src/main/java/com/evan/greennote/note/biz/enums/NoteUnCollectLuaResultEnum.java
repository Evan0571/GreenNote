package com.evan.greennote.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum NoteUnCollectLuaResultEnum {
    NOT_EXIST(-1L),
    NOTE_COLLECTED(1L),
    NOTE_NOT_COLLECTED(0L),
    ;

    private final Long code;

    public static NoteUnCollectLuaResultEnum valueOf(Long code){
        for(NoteUnCollectLuaResultEnum noteUnCollectLuaResultEnum: NoteUnCollectLuaResultEnum.values()){
            if(Objects.equals(code, noteUnCollectLuaResultEnum.getCode())){
                return noteUnCollectLuaResultEnum;
            }
        }
        return null;
    }
}

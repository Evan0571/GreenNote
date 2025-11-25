package com.evan.greennote.note.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

//笔记点赞，执行Lua脚本
@Getter
@AllArgsConstructor
public enum NoteLikeLuaResultEnum {
    NOT_EXIST(-1L),
    NOTE_LIKED(1L),
    NOTE_LIKE_SUCCESS(0L),
    ;
    private final Long code;

    public static NoteLikeLuaResultEnum valueOf(Long code){
        for(NoteLikeLuaResultEnum noteLikeLuaResultEnum:NoteLikeLuaResultEnum.values()){
            if(Objects.equals(code,noteLikeLuaResultEnum.getCode())){
                return noteLikeLuaResultEnum;
            }
        }
        return null;
    }
}

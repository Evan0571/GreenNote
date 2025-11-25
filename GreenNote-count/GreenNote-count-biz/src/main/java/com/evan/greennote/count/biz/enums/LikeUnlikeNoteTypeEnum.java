package com.evan.greennote.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum LikeUnlikeNoteTypeEnum {
    LIKE(1),
    UNLIKE(0),
    ;
    private final Integer code;

    public static LikeUnlikeNoteTypeEnum valueOf(Integer code){
        for(LikeUnlikeNoteTypeEnum likeUnlikeNoteTypeEnum:LikeUnlikeNoteTypeEnum.values()){
            if(Objects.equals(code,likeUnlikeNoteTypeEnum.getCode()))
                return likeUnlikeNoteTypeEnum;
        }
        return null;
    }
}

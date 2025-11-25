package com.evan.greennote.count.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    FOLLOW(1),
    UNFOLLOW(0),
    ;
    private final Integer code;

    public static FollowUnfollowTypeEnum valueOf(Integer code){
        for(FollowUnfollowTypeEnum followUnfollowTypeEnum: FollowUnfollowTypeEnum.values()){
            if(Objects.equals(code,followUnfollowTypeEnum.getCode())){
                return followUnfollowTypeEnum;
            }
        }
        return null;
    }
}

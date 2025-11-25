package com.evan.greennote.user.relation.biz.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

//关注/取关类型枚举
@Getter
@AllArgsConstructor
public enum FollowUnfollowTypeEnum {
    FOLLOW(1),
    UNFOLLOW(0),
    ;
    private final Integer code;
}

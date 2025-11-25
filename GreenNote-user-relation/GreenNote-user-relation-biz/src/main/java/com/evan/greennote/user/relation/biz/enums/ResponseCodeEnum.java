package com.evan.greennote.user.relation.biz.enums;

import com.evan.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    //通用异常码
    SYSTEM_ERROR("RELATION-10000","出错啦，系统正在修复中..."),
    PARAM_NOT_VALID("RELATION-10001","参数错误"),
    //业务异常码
    CANT_FOLLOW_YOUR_SELF("RELATION-20001","无法关注自己"),
    FOLLOW_USER_NOT_EXISTED("RELATION-20002","关注的用户不存在"),
    FOLLOWING_COUNT_LIMIT("RELATION-20003","关注数已达上限,请先取关部分用户"),
    ALREADY_FOLLOWING("RELATION-20004","您已关注该用户"),
    CANT_UNFOLLOW_YOUR_SELF("RELATION-20005","无法取关自己"),
    NOT_FOLLOWED("RELATION-20006","您没有关注该用户，无法取关"),
    ;

    //异常码
    private final String errorCode;
    //错误信息
    private final String errorMessage;
}

package com.evan.greennote.note.biz.enums;

import com.evan.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

//响应异常码
@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {
    //通用异常码
    SYSTEM_ERROR("NOTE-10000","出错啦，正在修复中..."),
    PARAM_NOT_VALID("NOTE-10001","参数错误"),

    //业务异常码
    NOTE_TYPE_ERROR("NOTE-20000","未知的笔记类型"),
    NOTE_PUBLISH_FAIL("NOTE-20001","笔记发布失败"),
    NOTE_NOT_FOUND("NOTE-20002","笔记不存在"),
    NOTE_PRIVATE("NOTE-20003","作者已将该笔记设置仅自己可见"),
    NOTE_UPDATE_FAIL("NOTE-20004","笔记更新失败"),
    TOPIC_NOT_FOUND("NOTE-20005","未找到该话题" ),
    NOTE_CANT_VISIBLE_ONLY_ME("NOTE-20006","该笔记无法修改为仅自己可见"),
    NOTE_CANT_OPERATE("NOTE-20007","您无法操作该笔记"),
    NOTE_ALREADY_LIKED("NOTE-20008","您已经点赞过该笔记"),
    NOTE_LIKE_FAIL("NOTE-20009","点赞失败"),
    NOTE_NOT_LIKED("NOTE-20010","您没有点赞过该笔记,无法取消点赞"),
    NOTE_ALREADY_COLLECTED("NOTE-20011","您已经收藏过该笔记"),
    NOTE_NOT_COLLECTED("NOTE-20012","您没有收藏过该笔记,无法取消收藏"),
    ;

    //异常码
    private final String errorCode;
    //错误信息
    private final String errorMessage;
}

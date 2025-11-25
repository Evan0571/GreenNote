package com.evan.greennote.oss.biz.enums;

import com.evan.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    //通用异常
    SYSTEM_ERROR("OSS-10000", "出错啦，正在努力修复中..."),
    PARAM_NOT_VALID("OSS-10001", "参数错误"),

    //业务异常
    ;

    //异常码
    private final String errorCode;
    //异常信息
    private final String errorMessage;
}

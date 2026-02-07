package com.evan.greennote.gateway.enums;

import com.evan.framework.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("500","系统繁忙，请稍后再试"),
    UNAUTHORIZED("401","权限不足"),
    ;

    private final String errorCode;
    private final String errorMessage;

    @Override
    public String getErrorCode() {
        return this.errorCode;
    }

    @Override
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
package com.evan.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusEnum {
    ENABLE(0),
    DISABLE(1);
    private final Integer value;
}

package com.evan.framework.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeleteEnum {
    YES(true),
    NO(false);
    private final Boolean value;
}

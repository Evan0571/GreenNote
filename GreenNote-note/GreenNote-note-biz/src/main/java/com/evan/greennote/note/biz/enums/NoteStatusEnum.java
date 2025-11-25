package com.evan.greennote.note.biz.enums;

//笔记状态

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NoteStatusEnum {

    BE_EXAMINE(0), //待审核
    NORMAL(1), //正常
    DELETED(2), //被删除
    DOWNED(3); //已下架

    private final Integer code;
}

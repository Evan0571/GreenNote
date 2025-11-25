package com.evan.greennote.note.biz.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

//日期工具类
public class DateUtils {
    //LocalDateTime 转时间戳
    public static Long localDateTime2Timestamp(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}

package com.evan.greennote.data.align.constant;

public class TableConstants {
    //表名中的分隔符
    public static final String TABLE_NAME_SEPARATOR="_";
    //拼接表名后缀
    public static String buildTableNameSuffix(String date, long hashKey){
        return date+TABLE_NAME_SEPARATOR+hashKey;
    }
}

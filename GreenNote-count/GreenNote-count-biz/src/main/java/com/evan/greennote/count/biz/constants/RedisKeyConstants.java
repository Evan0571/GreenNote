package com.evan.greennote.count.biz.constants;

public class RedisKeyConstants {
    private static final String COUNT_USER_KEY_PREFIX="count:user:";
    public static final String FIELD_FANS_TOTAL="fansTotal";
    //笔记维度Key前缀
    private static final String COUNT_NOTE_KEY_PREFIX="count:note:";
    //用户维度计数Key
    public static String buildCountUserKey(Long userId){
        return COUNT_USER_KEY_PREFIX+userId;
    }
    //笔记维度计数Key
    public static String buildCountNoteKey(Long noteId){
        return COUNT_NOTE_KEY_PREFIX+noteId;
    }
    //HashField:关注总数
    public static final String FIELD_FOLLOWING_TOTAL="followingTotal";
    //HashField:笔记点赞总数
    public static final String FIELD_LIKE_TOTAL="LikeTotal";
    //HashField:笔记收藏总数
    public static final String FIELD_COLLECT_TOTAL="collectTotal";
    //HashField:笔记发布总数
    public static final String FIELD_NOTE_TOTAL="noteTotal";
}

package com.evan.greennote.data.align.constant;

public class RedisKeyConstants {
    //布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞（笔记ID） 前缀
    public static final String BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY = "bloom:dataAlign:note:like:noteIds";
    //布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞（笔记发布者ID） 前缀
    public static final String BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY = "bloom:dataAlign:note:like:userIds";
    //布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏（笔记ID） 前缀
    public static final String BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY = "bloom:dataAlign:note:collect:noteIds";
    //布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏（笔记发布者ID） 前缀
    public static final String BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY = "bloom:dataAlign:note:collect:userIds";
    //布隆过滤器：日增量变更数据，用户笔记发布，取消发布 前缀
    public static final String BLOOM_TODAY_USER_NOTE_OPERATE_LIST_KEY = "bloom:dataAlign:note:operators:";
    //布隆过滤器：日增量变更数据，用户关注数 前缀
    public static final String BLOOM_TODAY_USER_FOLLOW_LIST_KEY = "bloom:dataAlign:user:follows:";
    //布隆过滤器：日增量变更数据，用户粉丝数 前缀
    public static final String BLOOM_TODAY_USER_FANS_LIST_KEY = "bloom:dataAlign:user:fans:";
    //用户维度计数key前缀
    private static final String COUNT_USER_KEY_PREFIX="count:user:";
    //笔记维度计数 Key 前缀
    private static final String COUNT_NOTE_KEY_PREFIX = "count:note:";
    //Hash Field:关注总数
    public static final String FIELD_FOLLOWING_TOTAL="followingTotal";
    //Hash Field: 笔记点赞总数
    public static final String FIELD_LIKE_TOTAL = "likeTotal";
    //Hash Field: 笔记收藏总数
    public static final String FIELD_COLLECT_TOTAL = "collectTotal";
    //Hash Field: 粉丝总数
    public static final String FIELD_FANS_TOTAL = "fansTotal";
    //Hash Field: 笔记发布总数
    public static final String FIELD_NOTE_TOTAL = "noteTotal";

    //构建完整的布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞(笔记ID) KEY
    public static String buildBloomUserNoteLikeNoteIdListKey(String date) {
        return BLOOM_TODAY_NOTE_LIKE_NOTE_ID_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户笔记点赞，取消点赞(笔记发布者ID) KEY
    public static String buildBloomUserNoteLikeUserIdListKey(String date) {
        return BLOOM_TODAY_NOTE_LIKE_USER_ID_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏(笔记ID) KEY
    public static String buildBloomUserNoteCollectNoteIdListKey(String date){
        return BLOOM_TODAY_NOTE_COLLECT_NOTE_ID_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户笔记收藏，取消收藏(笔记发布者ID) KEY
    public static String buildBloomUserNoteCollectUserIdListKey(String date) {
        return BLOOM_TODAY_NOTE_COLLECT_USER_ID_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户笔记发布，取消发布 KEY
    public static String buildBloomUserNoteOperateListKey(String date) {
        return BLOOM_TODAY_USER_NOTE_OPERATE_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户关注数 KEY
    public static String buildBloomUserFollowListKey(String date) {
        return BLOOM_TODAY_USER_FOLLOW_LIST_KEY + date;
    }
    //构建完整的布隆过滤器：日增量变更数据，用户粉丝数 KEY
    public static String buildBloomUserFansListKey(String date) {
        return BLOOM_TODAY_USER_FANS_LIST_KEY + date;
    }
    //构建用户维度计数key
    public static String buildCountUserKey(Long userId) {
        return COUNT_USER_KEY_PREFIX + userId;
    }
    //构建笔记维度计数 Key
    public static String buildCountNoteKey(Long noteId) {
        return COUNT_NOTE_KEY_PREFIX + noteId;
    }
}

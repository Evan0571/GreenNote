package com.evan.greennote.note.biz.constant;

public class RedisKeyConstants {
    //笔记详情KEY前缀
    public static final String NOTE_DETAIL_KEY="note:detail:";
    //布隆过滤器: 用户笔记点赞列表
    public static final String BLOOM_USER_NOTE_LIKE_LIST_KEY="bloom:note:likes:";
    //布隆过滤器: 用户笔记收藏列表
    public static final String BLOOM_USER_NOTE_COLLECT_LIST_KEY="bloom:note:collects:";
    //用户笔记点赞列表ZSet前缀
    public static final String USER_NOTE_LIKE_ZSET_KEY="user:note:likes:";
    //用户笔记收藏列表ZSet前缀
    public static final String USER_NOTE_COLLECT_ZSET_KEY="user:note:collects:";
    //构建完整的笔记详情KEY
    public static String buildNoteDetailKey(Long noteId){
        return NOTE_DETAIL_KEY+noteId;
    }
    //构建布隆过滤器: 用户点赞KEY
    public static String buildBloomUserNoteLikeListKey(Long userId){
        return BLOOM_USER_NOTE_LIKE_LIST_KEY+userId;
    }
    //构建布隆过滤器: 用户收藏KEY
    public static String buildBloomUserNoteCollectListKey(Long userId){
        return BLOOM_USER_NOTE_COLLECT_LIST_KEY+userId;
    }
    //构建用户笔记点赞列表ZSet KEY
    public static String buildUserNoteLikeZSetKey(Long userId){
        return USER_NOTE_LIKE_ZSET_KEY+userId;
    }
    //构建用户笔记收藏列表ZSet KEY
    public static String buildUserNoteCollectZSetKey(Long userId){
        return USER_NOTE_COLLECT_ZSET_KEY+userId;
    }
}

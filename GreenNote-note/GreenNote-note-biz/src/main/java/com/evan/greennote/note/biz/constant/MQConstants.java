package com.evan.greennote.note.biz.constant;

public interface MQConstants {
    //topic:删除笔记本地缓存
    String TOPIC_DELETE_NOTE_LOCAL_CACHE="DeleteNoteLocalCacheTopic";
    //topic:延迟双删Redis笔记缓存
    String TOPIC_DELAY_DELETE_NOTE_REDIS_CACHE="DelayDeleteNoteRedisCacheTopic";
    //topic:点赞/取消点赞
    String TOPIC_LIKE_OR_UNLIKE="LikeUnlikeTopic";
    //topic:收藏/取消收藏
    String TOPIC_COLLECT_OR_UN_COLLECT="CollectUnCollectTopic";
    //topic:计数-笔记点赞数
    String TOPIC_COUNT_NOTE_LIKE="CountNoteLikeTopic";
    //topic:计数-笔记收藏数
    String TOPIC_COUNT_NOTE_COLLECT="CountNoteCollectTopic";
    //topic:笔记操作（发布/删除）
    String TOPIC_NOTE_OPERATE="NoteOperateTopic";
    //topic:延迟双删 Redis 已发布笔记列表缓存
    String TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE = "DelayDeletePublishedNoteListRedisCacheTopic";
    //topic:发布笔记事务消息
    String TOPIC_PUBLISH_NOTE_TRANSACTION = "PublishNoteTransactionTopic";
    //点赞标签
    String TAG_LIKE="Like";
    //取消点赞标签
    String TAG_UNLIKE="Unlike";
    //收藏标签
    String TAG_COLLECT="Collect";
    //取消收藏标签
    String TAG_UN_COLLECT="UnCollect";
    //发布标签
    String TAG_NOTE_PUBLISH="publishNote";
    //删除标签
    String TAG_NOTE_DELETE="deleteNote";
}

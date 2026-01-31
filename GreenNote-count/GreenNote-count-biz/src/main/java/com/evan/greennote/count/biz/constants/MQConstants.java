package com.evan.greennote.count.biz.constants;

public interface MQConstants {
    //Topic: 关注数计数
    String TOPIC_COUNT_FOLLOWING="CountFollowingTopic";
    //Topic: 粉丝数计数
    String TOPIC_COUNT_FANS="CountFansTopic";
    //Topic: 粉丝数计数入库
    String TOPIC_COUNT_FANS_2_DB="CountFans2DBTopic";
    //Topic: 关注数计数入库
    String TOPIC_COUNT_FOLLOWING_2_DB="CountFollowing2DBTopic";
    //Topic: 计数-笔记点赞数
    String TOPIC_COUNT_NOTE_LIKE="CountNoteLikeTopic";
    //Topic: 计数-笔记点赞数入库
    String TOPIC_COUNT_NOTE_LIKE_2_DB="CountNoteLike2DBTopic";
    //Topic: 计数-笔记收藏数
    String TOPIC_COUNT_NOTE_COLLECT="CountNoteCollectTopic";
    //Topic: 计数-笔记收藏数入库
    String TOPIC_COUNT_NOTE_COLLECT_2_DB="CountNoteCollect2DBTopic";
    //Topic: 笔记操作（发布/删除）
    String TOPIC_NOTE_OPERATE="NoteOperateTopic";
    //Topic: 计数 - 笔记点赞数
    String TOPIC_LIKE_OR_UNLIKE = "LikeUnlikeTopic";
    //Topic: 笔记评论总数计数
    String TOPIC_COUNT_NOTE_COMMENT = "CountNoteCommentTopic";
    //Topic: 评论热度值更新
    String TOPIC_COMMENT_HEAT_UPDATE = "CommentHeatUpdateTopic";
    //笔记发布标签
    String TAG_NOTE_PUBLISH="publishNote";
    //笔记删除标签
    String TAG_NOTE_DELETE="deleteNote";
}

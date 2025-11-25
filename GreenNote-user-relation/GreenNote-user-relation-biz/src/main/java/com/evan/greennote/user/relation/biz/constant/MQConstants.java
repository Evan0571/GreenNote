package com.evan.greennote.user.relation.biz.constant;

//MQ常量
public interface MQConstants {
    //Topic: 关注/取关共用一个
    String TOPIC_FOLLOW_OR_UNFOLLOW="FollowUnFollowTopic";
    //Topic: 关注计数
    String TOPIC_COUNT_FOLLOWING="CountFollowingTopic";
    //Topic: 粉丝计数
    String TOPIC_COUNT_FANS="CountFansTopic";
    //关注标签
    String TAG_FOLLOW="Follow";
    //取关标签
    String TAG_UNFOLLOW="UnFollow";
}

package com.evan.greennote.note.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.note.biz.constant.MQConstants;
import com.evan.greennote.note.biz.constant.RedisKeyConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

//延时删除 Redis 已发布笔记列表缓存
@Component
@Slf4j
@RocketMQMessageListener(consumerGroup = "greennote_group_" + MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE, // Group
        topic = MQConstants.TOPIC_DELAY_DELETE_PUBLISHED_NOTE_LIST_REDIS_CACHE // 消费的主题 Topic
)
public class DelayDeletePublishedNoteListRedisCacheConsumer implements RocketMQListener<String> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onMessage(String body) {
        try {
            List<Long> noteIdAndUserId = JsonUtils.parseList(body, Long.class);

            Long noteId = noteIdAndUserId.get(0);
            Long userId = noteIdAndUserId.get(1);
            log.info("## 延迟消息消费成功, noteId: {}, userId: {}", noteId, userId);

            // 删除 Redis 笔记缓存
            String noteDetailRedisKey = RedisKeyConstants.buildNoteDetailKey(noteId);
            // 删除个人主页 - 已发布笔记列表缓存
            String publishedNoteListRedisKey = RedisKeyConstants.buildPublishedNoteListKey(userId);
            // 批量删除
            redisTemplate.delete(Arrays.asList(noteDetailRedisKey, publishedNoteListRedisKey));
        } catch (Exception e) {
            log.error("", e);
        }
    }
}

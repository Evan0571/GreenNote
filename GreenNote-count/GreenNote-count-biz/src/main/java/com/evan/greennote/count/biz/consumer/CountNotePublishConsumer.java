package com.evan.greennote.count.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.constants.RedisKeyConstants;
import com.evan.greennote.count.biz.domain.mapper.UserCountDOMapper;
import com.evan.greennote.count.biz.model.dto.NoteOperateMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;

//笔记发布数计数
@Component
@RocketMQMessageListener(consumerGroup = "greennote_group_"+ MQConstants.TOPIC_NOTE_OPERATE,
        topic=MQConstants.TOPIC_NOTE_OPERATE)
@Slf4j
public class CountNotePublishConsumer implements RocketMQListener<Message> {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private UserCountDOMapper userCountDOMapper;

    @Override
    public void onMessage(Message message) {
        // 消息体
        String bodyJsonStr = new String(message.getBody());
        // 标签
        String tags = message.getTags();

        log.info("==> CountNotePublishConsumer 消费了消息 {}, tags: {}", bodyJsonStr, tags);

        // 根据 MQ 标签，判断笔记操作类型
        if (Objects.equals(tags, MQConstants.TAG_NOTE_PUBLISH)) { // 笔记发布
            handleTagMessage(bodyJsonStr,1);
        } else if (Objects.equals(tags, MQConstants.TAG_NOTE_DELETE)) { // 笔记删除
            handleTagMessage(bodyJsonStr,-1);
        }
    }

    //笔记发布/删除
    private void handleTagMessage(String bodyJsonStr,long count){
        //消息体Json转DTO
        NoteOperateMqDTO noteOperateMqDTO = JsonUtils.parseObject(bodyJsonStr, NoteOperateMqDTO.class);
        if(Objects.isNull(noteOperateMqDTO)) return;
        Long creatorId = noteOperateMqDTO.getCreatorId();
        //更新Redis中用户维度的计数hash
        String countUserRedisKey = RedisKeyConstants.buildCountUserKey(creatorId);
        //存在则更新
        if(count!=0){
            redisTemplate.opsForHash().increment(countUserRedisKey,RedisKeyConstants.FIELD_NOTE_TOTAL,count);
        }
        log.debug("Updating user note count in Redis: userId={}, count={}", creatorId, count);
        //更新t_user_count表
        userCountDOMapper.insertOrUpdateNoteTotalByUserId(count,creatorId);
    }

}

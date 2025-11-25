package com.evan.greennote.note.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.note.biz.constant.MQConstants;
import com.evan.greennote.note.biz.domain.dataobject.NoteCollectionDO;
import com.evan.greennote.note.biz.domain.mapper.NoteCollectionDOMapper;
import com.evan.greennote.note.biz.model.dto.CollectUnCollectNoteMqDTO;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

//笔记收藏/取消收藏MQ消费者
@Component
@RocketMQMessageListener(consumerGroup = "greennote_group_"+ MQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
        topic = MQConstants.TOPIC_COLLECT_OR_UN_COLLECT,
        consumeMode=ConsumeMode.ORDERLY)
@Slf4j
public class CollectUnCollectNoteConsumer implements RocketMQListener<Message> {

    @Resource
    private NoteCollectionDOMapper noteCollectionDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private RateLimiter rateLimiter= RateLimiter.create(5000);

    @Override
    public void onMessage(Message message) {
        //流量削峰：通过可获得令牌，如果没有令牌可用将阻塞直到获得令牌
        rateLimiter.acquire();

        //幂等性: 通过联合唯一索引保证

        //消息体
        String bodyJsonStr=new String(message.getBody());
        //标签
        String tags=message.getTags();

        log.info("==> CollectUnCollectNoteConsumer 消费了消息 {}, tags: {}", bodyJsonStr,tags);

        //根据MQ标签，判断操作类型
        if(Objects.equals(tags,MQConstants.TAG_COLLECT)){
            handleCollectNoteTagMessage(bodyJsonStr);
        }else if(Objects.equals(tags,MQConstants.TAG_UN_COLLECT)){
            handleUnCollectNoteTagMessage(bodyJsonStr);
        }
    }

    //笔记收藏
    private void handleCollectNoteTagMessage(String bodyJsonStr){
        //消息体JSON字符串转DTO
        CollectUnCollectNoteMqDTO collectUnCollectNoteMqDTO= JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMqDTO.class);
        if(Objects.isNull(collectUnCollectNoteMqDTO))
            return;

        //用户ID
        Long userId=collectUnCollectNoteMqDTO.getUserId();
        //笔记ID
        Long noteId=collectUnCollectNoteMqDTO.getNoteId();
        //操作类型
        Integer type=collectUnCollectNoteMqDTO.getType();
        //点赞时间
        LocalDateTime createTime=collectUnCollectNoteMqDTO.getCreateTime();

        //构建DO对象
        NoteCollectionDO noteCollectionDO=NoteCollectionDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        //添加/更新笔记点赞记录
        int count=noteCollectionDOMapper.insertOrUpdate(noteCollectionDO);

        if(count==0) return;

        //更新数据库成功后，发送计数MQ
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(bodyJsonStr).build();

        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数：笔记收藏] MQ发送成功， SendResult:{}", sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数：笔记收藏] MQ发送失败", throwable);
            }
        });

    }

    //笔记取消收藏
    private void handleUnCollectNoteTagMessage(String bodyJsonStr){
        // 消息体 JSON 字符串转 DTO
        CollectUnCollectNoteMqDTO unCollectNoteMqDTO = JsonUtils.parseObject(bodyJsonStr, CollectUnCollectNoteMqDTO.class);

        if (Objects.isNull(unCollectNoteMqDTO)) return;

        // 用户ID
        Long userId = unCollectNoteMqDTO.getUserId();
        // 收藏的笔记ID
        Long noteId = unCollectNoteMqDTO.getNoteId();
        // 操作类型
        Integer type = unCollectNoteMqDTO.getType();
        // 收藏时间
        LocalDateTime createTime = unCollectNoteMqDTO.getCreateTime();

        // 构建 DO 对象
        NoteCollectionDO noteCollectionDO = NoteCollectionDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        // 取消收藏,记录更新
        int count = noteCollectionDOMapper.update2UnCollectByUserIdAndNoteId(noteCollectionDO);

        if (count == 0) return;

        //更新完数据库后，发送计数 MQ
        org.springframework.messaging.Message<String> message = MessageBuilder.withPayload(bodyJsonStr).build();

        // 异步发送 MQ 消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_COLLECT, message, new SendCallback() {
           @Override
           public void onSuccess(SendResult sendResult) {
               log.info("==> [计数：笔记取消收藏] MQ发送成功， SendResult:{}", sendResult);
           }
           @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数：笔记取消收藏] MQ发送失败", throwable);
            }
        });
    }
}

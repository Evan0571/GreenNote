package com.evan.greennote.note.biz.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.note.biz.constant.MQConstants;
import com.evan.greennote.note.biz.domain.dataobject.NoteLikeDO;
import com.evan.greennote.note.biz.domain.mapper.NoteLikeDOMapper;
import com.evan.greennote.note.biz.model.dto.LikeUnlikeNoteMqDTO;
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

//笔记点赞/取消点赞MQ消费者
@Component
@RocketMQMessageListener(consumerGroup = "greennote_group_" + MQConstants.TOPIC_LIKE_OR_UNLIKE,
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE,
        consumeMode= ConsumeMode.ORDERLY)
@Slf4j
public class LikeUnlikeNoteConsumer implements RocketMQListener<Message> {
    @Resource
    private NoteLikeDOMapper noteLikeDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private RateLimiter rateLimiter=RateLimiter.create(5000);

    @Override
    public void onMessage(Message message) {
        //流量削峰：通过可获得令牌，如果没有令牌可用将阻塞直到获得令牌
        rateLimiter.acquire();

        //幂等性: 通过联合唯一索引保证

        //消息体
        String bodyJsonStr=new String(message.getBody());
        //标签
        String tags=message.getTags();

        log.info("==> LikeUnlikeNoteConsumer 消费了消息 {}, tags: {}", bodyJsonStr,tags);

        //根据MQ标签，判断操作类型
        if(Objects.equals(tags,MQConstants.TAG_LIKE)){
            handleLikeNoteTagMessage(bodyJsonStr);
        }else if(Objects.equals(tags,MQConstants.TAG_UNLIKE)){
            handleUnlikeNoteTagMessage(bodyJsonStr);
        }
    }
    //处理点赞笔记消息
    private void handleLikeNoteTagMessage(String bodyJsonStr){
        //消息体JSON字符串转DTO
        LikeUnlikeNoteMqDTO likeNoteMqDTO= JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);
        if(Objects.isNull(likeNoteMqDTO))
            return;

        //用户ID
        Long userId=likeNoteMqDTO.getUserId();
        //笔记ID
        Long noteId=likeNoteMqDTO.getNoteId();
        //操作类型
        Integer type=likeNoteMqDTO.getType();
        //点赞时间
        LocalDateTime createTime=likeNoteMqDTO.getCreateTime();

        //构建DO对象
        NoteLikeDO noteLikeDO=NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        //添加/更新笔记点赞记录
        int count=noteLikeDOMapper.insertSelective(noteLikeDO);
        if(count==0)
            return;
        //更新数据库后，发送计数MQ
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(bodyJsonStr).build();
        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数：笔记点赞] MQ发送成功, SendResult:{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数：笔记点赞] MQ发送异常, throwable:{}",throwable);
            }
        });
    }
    //处理取消点赞笔记消息
    private void handleUnlikeNoteTagMessage(String bodyJsonStr){
        //消息体JSON字符串转DTO
        LikeUnlikeNoteMqDTO unlikeNoteMqDTO= JsonUtils.parseObject(bodyJsonStr, LikeUnlikeNoteMqDTO.class);
        if(Objects.isNull(unlikeNoteMqDTO))
            return;
        //用户ID
        Long userId=unlikeNoteMqDTO.getUserId();
        //笔记ID
        Long noteId=unlikeNoteMqDTO.getNoteId();
        //操作类型
        Integer type=unlikeNoteMqDTO.getType();
        //点赞时间
        LocalDateTime createTime=unlikeNoteMqDTO.getCreateTime();

        //构建DO对象
        NoteLikeDO noteLikeDO=NoteLikeDO.builder()
                .userId(userId)
                .noteId(noteId)
                .createTime(createTime)
                .status(type)
                .build();

        //删除笔记点赞记录
        int count=noteLikeDOMapper.update2UnlikeByUserIdAndNoteId(noteLikeDO);
        if (count==0)
            return;
        //更新数据库后，发送计数MQ
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(bodyJsonStr).build();
        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数：笔记取消点赞] MQ发送成功, SendResult:{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数：笔记取消点赞] MQ发送异常, throwable:{}",throwable);
            }
        });
    }
}

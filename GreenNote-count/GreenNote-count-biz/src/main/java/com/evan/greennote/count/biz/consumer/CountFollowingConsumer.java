package com.evan.greennote.count.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.constants.RedisKeyConstants;
import com.evan.greennote.count.biz.enums.FollowUnfollowTypeEnum;
import com.evan.greennote.count.biz.model.dto.CountFollowUnfollowMqDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.Objects;

//关注数计数
@Component
@RocketMQMessageListener(consumerGroup = "greennote_group_"+ MQConstants.TOPIC_COUNT_FOLLOWING,
        topic = MQConstants.TOPIC_COUNT_FOLLOWING)
@Slf4j
public class CountFollowingConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(String body) {
        log.info("## 消费者消费成功, 消费MQ[关注数计数], body:{}",body);
        if(StringUtils.isBlank(body))
            return;
        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO= JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class);
        //关注/取关
        Integer type=countFollowUnfollowMqDTO.getType();
        //原用户ID
        Long userId=countFollowUnfollowMqDTO.getUserId();
        //更新Redis
        String redisKey= RedisKeyConstants.buildCountUserKey(userId);
        //判断Hash是否存在
        boolean isExisted=redisTemplate.hasKey(redisKey);
        //若存在
        if (isExisted) {
            long count= Objects.equals(type, FollowUnfollowTypeEnum.FOLLOW.getCode())?1:-1;
            //对Hash中followingTotal字段进行加减操作
            redisTemplate.opsForHash().increment(redisKey,RedisKeyConstants.FIELD_FOLLOWING_TOTAL,count);
        }
        //发送MQ，关注数写库
        //构建消息对象
        Message<String> message= MessageBuilder.withPayload(body)
                .build();

        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：关注数】MQ发送成功，SendResult: {}", sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：关注数】MQ发送异常: ", throwable);
            }
        });
    }
}

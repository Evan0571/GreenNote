package com.evan.greennote.count.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.domain.mapper.UserCountDOMapper;
import com.evan.greennote.count.biz.enums.FollowUnfollowTypeEnum;
import com.evan.greennote.count.biz.model.dto.CountFollowUnfollowMqDTO;
import com.google.common.util.concurrent.RateLimiter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

//计数：关注数入库
@Component
@RocketMQMessageListener(consumerGroup="greennote_group_"+ MQConstants.TOPIC_COUNT_FOLLOWING_2_DB,
        topic = MQConstants.TOPIC_COUNT_FOLLOWING_2_DB)
@Slf4j
public class CountFollowing2DBConsumer implements RocketMQListener<String> {
    @Resource
    private UserCountDOMapper userCountDOMapper;
    private RateLimiter rateLimiter = RateLimiter.create(5000);

    @Override
    public void onMessage(String body){
        //流量削峰：通过获得令牌，若没有令牌则堵塞直到获得
        rateLimiter.acquire();
        log.info("## 消费到了 MQ【计数：关注数入库】,{}...",body);
        if(StringUtils.isBlank(body))
            return;
        CountFollowUnfollowMqDTO countFollowUnfollowMqDTO= JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class);
        //关注/取关
        Integer type=countFollowUnfollowMqDTO.getType();
        //原用户ID
        Long userId=countFollowUnfollowMqDTO.getUserId();

        int count= Objects.equals(type, FollowUnfollowTypeEnum.FOLLOW.getCode())?1:-1;
        //判断数据库是否有目标用户记录，若不存在，则插入，若已存在，则直接更新
        userCountDOMapper.insertOrUpdateFollowingTotalByUserId(count,userId);
    }
}

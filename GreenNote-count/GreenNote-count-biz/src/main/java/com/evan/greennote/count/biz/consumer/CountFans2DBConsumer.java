package com.evan.greennote.count.biz.consumer;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.domain.mapper.UserCountDOMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

//计数：粉丝数入库
@Component
@RocketMQMessageListener(consumerGroup="greennote_group_"+ MQConstants.TOPIC_COUNT_FANS_2_DB,
        topic=MQConstants.TOPIC_COUNT_FANS_2_DB)
@Slf4j
public class CountFans2DBConsumer implements RocketMQListener<String> {
    private final UserCountDOMapper userCountDOMapper;
    private RateLimiter rateLimiter = RateLimiter.create(5000);

    public CountFans2DBConsumer(UserCountDOMapper userCountDOMapper) {
        this.userCountDOMapper = userCountDOMapper;
    }

    @Override
    public void onMessage(String body){
        //流量削峰：通过获得令牌，若没有令牌则堵塞直到获得
        rateLimiter.acquire();
        log.info("## 消费到了 MQ【计数：粉丝数入库】,{}...",body);

        Map<Long,Integer> countMap=null;
        try{
            countMap= JsonUtils.parseMap(body, Long.class, Integer.class);
        } catch (Exception e) {
            log.error("## 解析Json字符串异常",e);
        }
        if(CollUtil.isNotEmpty(countMap)){
            //判断数据库是否有目标用户记录，若不存在，则插入，若已存在，则直接更新
            countMap.forEach((k,v)->userCountDOMapper.insertOrUpdateFansTotalByUserId(v,k));
        }
    }
}

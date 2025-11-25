package com.evan.greennote.count.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.constants.RedisKeyConstants;
import com.evan.greennote.count.biz.enums.FollowUnfollowTypeEnum;
import com.evan.greennote.count.biz.model.dto.CountFollowUnfollowMqDTO;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Maps;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

//粉丝数计数
@Component
@RocketMQMessageListener(consumerGroup="greennote_group_"+ MQConstants.TOPIC_COUNT_FANS,
        topic=MQConstants.TOPIC_COUNT_FANS)
@Slf4j
public class CountFansConsumer implements RocketMQListener<String> {
    private volatile long totalMessages = 0;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private BufferTrigger<String> bufferTrigger = BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofMillis(500))
            .setConsumerEx(this::consumeMessage)
            .build();
    @Override
    public void onMessage(String body){
        //往bufferTrigger中添加元素
        bufferTrigger.enqueue(body);
    }
    private void consumeMessage(List<String> bodies){
        try {
            log.info("==> 聚合消息，size:{}",bodies.size());
            log.info("==> 聚合消息，{}",JsonUtils.toJsonString(bodies));
            totalMessages+=bodies.size();
            log.info("==> 总消息数：{}", totalMessages);
        } catch (Exception e) {
            log.error("==> 消息处理异常: ", e);
        }

        //List<String>转List<CountFollowUnfollowMqDTO>
        List<CountFollowUnfollowMqDTO> countFollowUnfollowMqDTOS=bodies.stream()
                .map(body->JsonUtils.parseObject(body, CountFollowUnfollowMqDTO.class))
                .toList();

        //按目标用户进行分组
        Map<Long,List<CountFollowUnfollowMqDTO>> groupMap=countFollowUnfollowMqDTOS.stream()
                .collect(Collectors.groupingBy(CountFollowUnfollowMqDTO::getTargetUserId));

        //按组汇总数据，统计出最终计数
        //key为目标用户id，value为最终操作的计数
        Map<Long,Integer> countMap= Maps.newHashMap();
        for(Map.Entry<Long,List<CountFollowUnfollowMqDTO>> entry:groupMap.entrySet()){
            List<CountFollowUnfollowMqDTO> list = entry.getValue();
            //统计出最终计数
            int finalCount=0;
            for(CountFollowUnfollowMqDTO countFollowUnfollowMqDTO:list){
                //获取操作类型
                Integer type = countFollowUnfollowMqDTO.getType();
                //根据类型获取对应枚举
                FollowUnfollowTypeEnum followUnfollowTypeEnum = FollowUnfollowTypeEnum.valueOf(type);
                //若枚举为空，跳到下一次循环
                if(Objects.isNull(followUnfollowTypeEnum))
                    continue;
                switch(followUnfollowTypeEnum){
                    case FOLLOW->finalCount+=1;
                    case UNFOLLOW->finalCount-=1;
                }
            }
            //将分组后统计出的的最终计数保存到countMap中
            countMap.put(entry.getKey(),finalCount);
        }
        log.info("## 聚合后的计数数据：{}",JsonUtils.toJsonString(countMap));

        // 更新粉丝ID集合
        countFollowUnfollowMqDTOS.forEach(dto -> {
            String fansSetKey = "fans:" + dto.getTargetUserId();
            if (FollowUnfollowTypeEnum.FOLLOW.getCode().equals(dto.getType())) {
                redisTemplate.opsForSet().add(fansSetKey, String.valueOf(dto.getUserId()));
            } else if (FollowUnfollowTypeEnum.UNFOLLOW.getCode().equals(dto.getType())) {
                redisTemplate.opsForSet().remove(fansSetKey, String.valueOf(dto.getUserId()));
            }
        });

        //更新redis
        countMap.forEach((k,v)->{
            //redis key
            String redisKey= RedisKeyConstants.buildCountUserKey(k);
            //判断redis中hash是否存在
            boolean isExisted=redisTemplate.hasKey(redisKey);
            //若存在才会更新
            if(isExisted){
                //对目标用户Hash中的粉丝字段进行计数操作
                redisTemplate.opsForHash().increment(redisKey,RedisKeyConstants.FIELD_FANS_TOTAL,v);
            }
        });

        //发送MQ，计数数据落库
        //构建消息体DTO
        Message<String> message = MessageBuilder.withPayload(JsonUtils.toJsonString(countMap))
                .build();
        //异步发送MQ消息,提升接口响应速度
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS_2_DB,message,new SendCallback(){
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> 【计数服务：粉丝数】发送成功，SendResult: {}", sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> 【计数服务：粉丝数】发送异常: ", throwable);
            }
        });
    }

    @PreDestroy
    public void preDestroy() {
        // 等待队列消费完毕再销毁 Bean
        if (bufferTrigger != null) {
            bufferTrigger.close();
        }
    }
}

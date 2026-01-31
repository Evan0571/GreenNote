package com.evan.greennote.count.biz.consumer;

import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.count.biz.constants.MQConstants;
import com.evan.greennote.count.biz.constants.RedisKeyConstants;
import com.evan.greennote.count.biz.enums.LikeUnlikeNoteTypeEnum;
import com.evan.greennote.count.biz.model.dto.AggregationCountLikeUnlikeNoteMqDTO;
import com.evan.greennote.count.biz.model.dto.CountLikeUnlikeNoteMqDTO;
import com.github.phantomthief.collection.BufferTrigger;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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

//点赞数计数--批量写库优化版
@Component
@RocketMQMessageListener(consumerGroup = "greennote_group_"+ MQConstants.TOPIC_LIKE_OR_UNLIKE, // Group 组
        topic = MQConstants.TOPIC_LIKE_OR_UNLIKE)
@Slf4j
public class CountNoteLikeConsumer implements RocketMQListener<String> {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    private BufferTrigger<String> bufferTrigger= BufferTrigger.<String>batchBlocking()
            .bufferSize(50000)
            .batchSize(1000)
            .linger(Duration.ofSeconds(1))
            .setConsumerEx(this::consumeMessage)
            .build();

    @Override
    public void onMessage(String body) {
        bufferTrigger.enqueue(body);
    }
    private void consumeMessage(List<String>bodies){
        log.info("==> [笔记点赞数]聚合消息，size: {}",bodies.size());
        log.info("==> [笔记点赞数]聚合消息，{}", JsonUtils.toJsonString(bodies));

        //List<String> 转 List<CountLikeUnlikeNoteMqDTO>
        List<CountLikeUnlikeNoteMqDTO> countLikeUnlikeNoteMqDTOS=bodies.stream()
                .map(body-> JsonUtils.parseObject(body, CountLikeUnlikeNoteMqDTO.class))
                .toList();

        //按笔记ID分组
        Map<Long,List<CountLikeUnlikeNoteMqDTO>> groupMap=countLikeUnlikeNoteMqDTOS.stream()
                .collect(Collectors.groupingBy(CountLikeUnlikeNoteMqDTO::getNoteId));

        //按组汇总数据，统计出最终计数
        //Key为笔记ID，Value为点赞数
        Map<Long, Integer> countMap= Maps.newHashMap();

        List<AggregationCountLikeUnlikeNoteMqDTO> countList= Lists.newArrayList();

        for (Map.Entry<Long, List<CountLikeUnlikeNoteMqDTO>> entry : groupMap.entrySet()) {
            Long noteId=entry.getKey();
            Long creatorId=null;
            List<CountLikeUnlikeNoteMqDTO> list=entry.getValue();
            //最终的计数值，默认0
            int finalCount=0;
            for (CountLikeUnlikeNoteMqDTO countLikeUnlikeNoteMqDTO : list) {
                creatorId=countLikeUnlikeNoteMqDTO.getNoteCreatorId();
                //获取操作类型
                Integer type=countLikeUnlikeNoteMqDTO.getType();
                //根据操作类型获取对应枚举
                LikeUnlikeNoteTypeEnum likeUnlikeNoteTypeEnum= LikeUnlikeNoteTypeEnum.valueOf(type);
                //若枚举为空，则跳到下一次循环
                if(Objects.isNull(likeUnlikeNoteTypeEnum))
                    continue;

                switch (likeUnlikeNoteTypeEnum){
                    case LIKE->finalCount+=1;
                    case UNLIKE->finalCount-=1;
                }
            }
            //将分组后统计出的数据保存到CountList中
            countList.add(AggregationCountLikeUnlikeNoteMqDTO.builder()
                    .noteId(noteId)
                    .creatorId(creatorId)
                    .count(finalCount)
                    .build());
        }
        log.info("##[笔记点赞数] 聚合后的计数数据：{}",JsonUtils.toJsonString(countList));

        //更新redis
        countList.forEach(item->{
            Long creatorId=item.getCreatorId();
            Long noteId=item.getNoteId();
            Integer count=item.getCount();
            String countNoteRedisKey=RedisKeyConstants.buildCountNoteKey(noteId);
            boolean isCountNoteExisted=redisTemplate.hasKey(countNoteRedisKey);
            //若存在，则更新
            if(isCountNoteExisted){
                //对目标用户Hash中的点赞数字段进行加减操作
                redisTemplate.opsForHash().increment(countNoteRedisKey,RedisKeyConstants.FIELD_LIKE_TOTAL,count);
            }
            String countUserRedisKey=RedisKeyConstants.buildCountUserKey(creatorId);
            boolean isCountUserExisted=redisTemplate.hasKey(countUserRedisKey);
            if(isCountUserExisted){
                //对目标用户Hash中的点赞数字段进行加减操作
                redisTemplate.opsForHash().increment(countUserRedisKey,RedisKeyConstants.FIELD_LIKE_TOTAL,count);
            }
        });

        //发送MQ，点赞数写库
        Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(countList))
                .build();
        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_NOTE_LIKE_2_DB, message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数服务：笔记点赞数入库] MQ发送成功，SendResult: {}", sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数服务：笔记点赞数入库] MQ发送异常: ", throwable);
            }
        });
    }
}

package com.evan.greennote.user.relation.biz.consumer;

import com.alibaba.nacos.shaded.com.google.common.util.concurrent.RateLimiter;
import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.user.relation.biz.constant.MQConstants;
import com.evan.greennote.user.relation.biz.constant.RedisKeyConstants;
import com.evan.greennote.user.relation.biz.domain.dataobject.FansDO;
import com.evan.greennote.user.relation.biz.domain.dataobject.FollowingDO;
import com.evan.greennote.user.relation.biz.domain.mapper.FansDOMapper;
import com.evan.greennote.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.evan.greennote.user.relation.biz.enums.FollowUnfollowTypeEnum;
import com.evan.greennote.user.relation.biz.model.dto.CountFollowUnfollowMqDTO;
import com.evan.greennote.user.relation.biz.model.dto.FollowUserMqDTO;
import com.evan.greennote.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.evan.greennote.user.relation.biz.util.DateUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;

//关注/取关 MQ 消费者
@Component
@RocketMQMessageListener(consumerGroup="greennote_group"+MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        topic= MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW,
        selectorExpression = "Follow||UnFollow",
        consumeMode = ConsumeMode.ORDERLY
)
@Slf4j
public class FollowUnfollowConsumer implements RocketMQListener<Message> {
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private FansDOMapper fansDOMapper;
    @Resource
    private RateLimiter rateLimiter;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public void onMessage(Message message) {
        //流量削峰：通过获取令牌，如果没有令牌可用将阻塞，直到获得令牌
        rateLimiter.acquire();
        //消息体
        String bodyJsonStr=new String(message.getBody());
        //标签
        String tags=message.getTags();

        log.info("==> FollowUnfollowConsumer 消费了消息{}，tags：{}",bodyJsonStr,tags);
        //根据MQ 标签，判断操作类型
        if(Objects.equals(tags, MQConstants.TAG_FOLLOW)){
            handleFollowTagMessage(bodyJsonStr);
        }else if(Objects.equals(tags, MQConstants.TAG_UNFOLLOW)){
            handleUnfollowTagMessage(bodyJsonStr);
        }
    }
    //关注
    private void handleFollowTagMessage(String bodyJsonStr){
        log.info("开始处理关注消息，消息内容：{}", bodyJsonStr);
        //将消息体Json字符串转化为DTO对象
        FollowUserMqDTO followUserMqDTO= JsonUtils.parseObject(bodyJsonStr, FollowUserMqDTO.class);
        //判空
        if(Objects.isNull(followUserMqDTO)){
            return;
        }
        //幂等性
        Long userId=followUserMqDTO.getUserId();
        Long followUserId=followUserMqDTO.getFollowUserId();
        LocalDateTime createTime=followUserMqDTO.getCreateTime();

        //编程式提交事务
        boolean isSuccess=Boolean.TRUE.equals(transactionTemplate.execute(status->{
            try{
                //关注成功需往数据库插两条记录
                //关注表：一条记录
                int count=followingDOMapper.insert(FollowingDO.builder()
                        .userId(userId)
                        .followingUserId(followUserId)
                        .createTime(createTime)
                        .build());
                //粉丝表：一条记录
                if(count>0){
                    fansDOMapper.insert(FansDO.builder()
                            .userId(followUserId)
                            .fansUserId(userId)
                            .createTime(createTime)
                            .build());
                }
                return true;
            }catch (Exception ex){
                status.setRollbackOnly();//标记事务为回滚
                log.error("",ex);
            }
            return false;
        }));
        log.info("## 数据库添加记录结果：{}",isSuccess);
        //若数据库操作成功，更新Redis中被关注用户的ZSet粉丝列表
        if(isSuccess){
            DefaultRedisScript<Long> script=new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_update_fans_zset.lua")));
            script.setResultType(Long.class);

            //时间戳
            Long timestamp= DateUtils.localDateTime2Timestamp(createTime);

            //构建未被关注用户的粉丝列表RedisKey
            String fansRedisKey= RedisKeyConstants.buildUserFansKey(followUserId);

            log.info("==> 更新Redis粉丝列表，键名：{}", fansRedisKey);

            //执行脚本
            redisTemplate.execute(script, Collections.singletonList(fansRedisKey),userId,timestamp);

            log.info("==> 执行Lua脚本更新粉丝列表，键名：{}", fansRedisKey);
            log.info("==> Lua脚本执行结果：{}", redisTemplate.execute(script, Collections.singletonList(fansRedisKey), userId, timestamp));

            //发送MQ通知计数服务，统计关注数
            //构建消息题DTO
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO=CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(followUserId)
                    .type(FollowUnfollowTypeEnum.FOLLOW.getCode())
                    .build();

            sendMQ(countFollowUnfollowMqDTO);
        }
    }

    //取关
    private void handleUnfollowTagMessage(String bodyJsonStr){
        //将消息体Json字符串转化为DTO对象
        UnfollowUserMqDTO unfollowUserMqDTO= JsonUtils.parseObject(bodyJsonStr, UnfollowUserMqDTO.class);

        //判空
        if(Objects.isNull(unfollowUserMqDTO)) return;

        Long userId=unfollowUserMqDTO.getUserId();
        Long unfollowUserId=unfollowUserMqDTO.getUnfollowUserId();
        LocalDateTime createTime=unfollowUserMqDTO.getCreateTime();

        //编程式提交事务
        boolean isSuccess=Boolean.TRUE.equals(transactionTemplate.execute(status->{
            try{
                //取关成功需要删除数据库两条记录
                //关注表一条记录
                int count=followingDOMapper.deleteByUserIdAndFollowingUserId(userId,unfollowUserId);
                //粉丝表一条记录
                if(count>0){
                    fansDOMapper.deleteByUserIdAndFansUserId(unfollowUserId,userId);
                }
                return true;
            }catch(Exception ex){
                status.setRollbackOnly();//标记事务为回滚
                log.error("",ex);
            }
            return false;
        }));

        //若数据库删除成功，更新redis，将自己从被取关用户的ZSET粉丝列表删除
        if(isSuccess){
            //被取关用户的粉丝列表RedisKey
            String fansRedisKey= RedisKeyConstants.buildUserFansKey(unfollowUserId);
            //删除指定粉丝
            redisTemplate.opsForZSet().remove(fansRedisKey,userId);

            //发送MQ通知计数服务，统计关注数
            //构建消息题DTO
            CountFollowUnfollowMqDTO countFollowUnfollowMqDTO=CountFollowUnfollowMqDTO.builder()
                    .userId(userId)
                    .targetUserId(unfollowUserId)
                    .type(FollowUnfollowTypeEnum.UNFOLLOW.getCode())
                    .build();

            sendMQ(countFollowUnfollowMqDTO);
        }
    }

    //发送MQ通知计数服务
    private void sendMQ(CountFollowUnfollowMqDTO countFollowUnfollowMqDTO){
        //构建消息对象，并将DTO转成Json字符串设置到消息体中
        org.springframework.messaging.Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(countFollowUnfollowMqDTO))
                .build();

        //异步发送MQ消息
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FOLLOWING,message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数服务：关注数] MQ发送成功，结果：{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数服务：关注数] MQ发送失败，异常：{}",throwable);
            }
        });

        //发送MQ通知计数服务:统计粉丝数
        rocketMQTemplate.asyncSend(MQConstants.TOPIC_COUNT_FANS,message, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==> [计数服务：粉丝数] MQ发送成功，结果：{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==> [计数服务：粉丝数] MQ发送失败，异常：{}",throwable);
            }
        });
    }
}

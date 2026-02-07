package com.evan.greennote.user.relation.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.evan.framework.biz.context.holder.LoginUserContextHolder;
import com.evan.framework.common.exception.BizException;
import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.framework.common.util.JsonUtils;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import com.evan.greennote.user.relation.biz.constant.MQConstants;
import com.evan.greennote.user.relation.biz.constant.RedisKeyConstants;
import com.evan.greennote.user.relation.biz.domain.dataobject.FansDO;
import com.evan.greennote.user.relation.biz.domain.dataobject.FollowingDO;
import com.evan.greennote.user.relation.biz.domain.mapper.FansDOMapper;
import com.evan.greennote.user.relation.biz.domain.mapper.FollowingDOMapper;
import com.evan.greennote.user.relation.biz.enums.LuaResultEnum;
import com.evan.greennote.user.relation.biz.enums.ResponseCodeEnum;
import com.evan.greennote.user.relation.biz.model.dto.FollowUserMqDTO;
import com.evan.greennote.user.relation.biz.model.dto.UnfollowUserMqDTO;
import com.evan.greennote.user.relation.biz.model.vo.*;
import com.evan.greennote.user.relation.biz.rpc.UserRpcService;
import com.evan.greennote.user.relation.biz.service.RelationService;
import com.evan.greennote.user.relation.biz.util.DateUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//用户业务
@Service
@Slf4j
public class RelationServiceImpl implements RelationService {
    @Resource
    private UserRpcService userRpcService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private FollowingDOMapper followingDOMapper;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private FansDOMapper fansDOMapper;

    //关注用户
    @Override
    public Response<?> follow(FollowUserReqVO followUserReqVO){
        //关注的用户 ID
        Long followUserId=followUserReqVO.getFollowUserId();
        //当前登录的用户 ID
        Long userId= LoginUserContextHolder.getUserId();
        //校验：用户无法关注自己
        if(Objects.equals(userId,followUserId)){
            throw new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }
        //校验: 关注的用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO=userRpcService.findById(followUserId);
        if(Objects.isNull(findUserByIdRspDTO)){
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }
        //构建当前用户关注列表的 Redis Key
        String followingRedisKey= RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        //Lua脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_check_and_add.lua")));
        //返回值类型
        script.setResultType(Long.class);
        //当前时间
        LocalDateTime now=LocalDateTime.now();
        //当前时间转时间戳
        long timestamp= DateUtils.localDateTime2Timestamp(now);
        //运行Lua脚本
        Long result=redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);
        //检验Lua脚本执行结果
        checkLuaScriptResult(result);
        //ZSET不存在
        if(Objects.equals(result, LuaResultEnum.ZSET_NOT_EXISTED.getCode())){
            //从数据库查询当前用户的关注关系记录
            List<FollowingDO> followingDOS=followingDOMapper.selectByUserId(userId);
            //随即过期时间，保底1天+随机秒
            long expireSeconds= 60*60*24+ RandomUtil.randomInt(60*60*24);
            //若记录为空，直接 ZADD 关系数据，并设置过期时间
            if(CollUtil.isEmpty(followingDOS)){
                DefaultRedisScript<Long> script2=new DefaultRedisScript<>();
                script2.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_add_and_expire.lua")));
                script2.setResultType(Long.class);
                // 可根据用户类型设置不同过期时长
                redisTemplate.execute(script2, Collections.singletonList(followingRedisKey), followUserId, timestamp, expireSeconds);
            }else{
                //若记录不为空，则将关注数据全量同步到Redis中，并设置过期时间
                //构建Lua参数
                Object[] luaArgs=buildLuaArgs(followingDOS, expireSeconds);
                //执行Lua脚本，批量同步关系数据到Redis
                DefaultRedisScript<Long> script3=new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                //再次调用上面的Lua脚本：follow_check_and_add.lua,添加最新关注关系
                result=redisTemplate.execute(script, Collections.singletonList(followingRedisKey), followUserId, timestamp);
                checkLuaScriptResult(result);
            }
        }

        //发送MQ
        //构建消息体 DTO
        FollowUserMqDTO followUserMqDTO=FollowUserMqDTO.builder()
                .userId(userId)
                .followUserId(followUserId)
                .createTime(now)
                .build();
        //构建消息对象，并将DTO转化成json字符串设置在消息体中
        Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(followUserMqDTO))
                .build();
        //通过冒号连接，可让MQ发送给主题Topic时，带上标签tag
        String destination= MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW+":"+MQConstants.TAG_FOLLOW;
        log.info("==>开始发送关注操作MQ，消息体：{}",followUserMqDTO);

        String hashKey=String.valueOf(userId);

        //异步发送MQ消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message, hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==>MQ发送成功，结果：{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==>MQ发送失败，异常：{}",throwable);
            }
        });

        return Response.success();
    }

    //校验Lua脚本执行结果，根据状态码抛出业务异常
    private static void checkLuaScriptResult(Long result){
        LuaResultEnum luaResultEnum=LuaResultEnum.getByCode(result);
        if(Objects.isNull(luaResultEnum)){
            throw new RuntimeException("Lua 返回结果错误");
        }
        //校验Lua脚本执行结果
        switch (luaResultEnum){
            //关注数已达上限
            case FOLLOW_LIMIT -> throw new BizException(ResponseCodeEnum.FOLLOWING_COUNT_LIMIT);
            //已经关注了该用户
            case ALREADY_FOLLOWED -> throw new BizException(ResponseCodeEnum.ALREADY_FOLLOWING);
        }
    }

    //构建 Lua 脚本参数
    private static Object[] buildLuaArgs(List<FollowingDO> followingDOS, long expireSeconds){
        int argsLength=followingDOS.size()*2+1; //每个关注关系有两个参数 score和value，再加一个过期时间
        Object[] luaArgs=new Object[argsLength];

        int i=0;
        for(FollowingDO following:followingDOS){
            luaArgs[i]=DateUtils.localDateTime2Timestamp(following.getCreateTime());//关注时间作为score
            luaArgs[i+1]=following.getFollowingUserId();//关注用户ID作为ZSet Value
            i+=2;
        }
        luaArgs[argsLength-1]=expireSeconds;//最后一个参数是ZSet的过期时间
        return luaArgs;
    }

    //取关用户
    @Override
    public Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO){
        //想取关的用户ID
        Long unfollowUserId=unfollowUserReqVO.getUnfollowUserId();
        //当前登录的用户ID
        Long userId=LoginUserContextHolder.getUserId();

        //无法取关自己
        if(Objects.equals(userId, unfollowUserId)){
            throw  new BizException(ResponseCodeEnum.CANT_FOLLOW_YOUR_SELF);
        }

        //检测取关用户是否存在
        FindUserByIdRspDTO findUserByIdRspDTO=userRpcService.findById(unfollowUserId);

        if(Objects.isNull(findUserByIdRspDTO)){
            throw new BizException(ResponseCodeEnum.FOLLOW_USER_NOT_EXISTED);
        }

        //检测要取关的用户是否已关注
        //当前用户关注列表 Redis Key
        String followingRedisKey=RedisKeyConstants.buildUserFollowingKey(userId);

        DefaultRedisScript<Long> script=new DefaultRedisScript<>();
        //Lua脚本路径
        script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/unfollow_check_and_delete.lua")));
        //返回值类型
        script.setResultType(Long.class);
        //执行Lua脚本
        Long result=redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);
        //校验Lua脚本执行结果
        //取关的用户不再关注列表中
        if(Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())){
            throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
        }

        //关注列表不存在
        if(Objects.equals(result,LuaResultEnum.ZSET_NOT_EXISTED.getCode())){
            //从数据库查询当前用户的关注关系记录
            List<FollowingDO> followingDOS=followingDOMapper.selectByUserId(userId);

            //随机过期时间
            //保底一天加随机秒数
            long expireSeconds=60*60*24+RandomUtil.randomInt(60*60*24);

            //若记录为空，则表示还未关注任何人，提示还未关注对方
            if(CollUtil.isEmpty(followingDOS)){
                throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
            }else{
                //若记录不为空，则将关注关系数据全量同步到redis中，并设置过期时间
                //构建Lua函数
                Object[] luaArgs=buildLuaArgs(followingDOS, expireSeconds);

                //执行Lua脚本，批量同步关注数据到redis中
                DefaultRedisScript<Long> script3=new DefaultRedisScript<>();
                script3.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
                script3.setResultType(Long.class);
                redisTemplate.execute(script3, Collections.singletonList(followingRedisKey), luaArgs);

                //再次调用上面的Lua脚本：unfollow_check_and_delete.lua,将取关用户删除
                result=redisTemplate.execute(script, Collections.singletonList(followingRedisKey), unfollowUserId);
                //再次校验结果
                if(Objects.equals(result, LuaResultEnum.NOT_FOLLOWED.getCode())){
                    throw new BizException(ResponseCodeEnum.NOT_FOLLOWED);
                }
            }

        }

        //发送MQ
        //构建消息体 DTO
        UnfollowUserMqDTO unfollowUserMqDTO=UnfollowUserMqDTO.builder()
                .userId(userId)
                .unfollowUserId(unfollowUserId)
                .createTime(LocalDateTime.now())
                .build();

        //构建消息对象，并将DTO转化成json字符串设置在消息体中
        Message<String> message= MessageBuilder.withPayload(JsonUtils.toJsonString(unfollowUserMqDTO))
                .build();

        //通过冒号连接，可让MQ发送给主题Topic时，带上标签tag
        String destination= MQConstants.TOPIC_FOLLOW_OR_UNFOLLOW+":"+MQConstants.TAG_UNFOLLOW;

        log.info("==>开始发送取关操作MQ，消息体：{}",unfollowUserMqDTO);

        String hashKey=String.valueOf(userId);

        //异步发送MQ消息，提升接口响应速度
        rocketMQTemplate.asyncSendOrderly(destination, message,hashKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("==>MQ发送成功，结果：{}",sendResult);
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("==>MQ发送失败，异常：{}",throwable);
            }
        });
        return Response.success();
    }

    //查询关注列表
    @Override
    public PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO){
        //想要查询的用户ID
        Long userId=findFollowingListReqVO.getUserId();
        //页码
        Integer pageNo=findFollowingListReqVO.getPageNo();
        //先从Redis中查询
        String followingListRedisKey=RedisKeyConstants.buildUserFollowingKey(userId);
        //查询目标用户关注列表ZSet总大小
        long total=redisTemplate.opsForZSet().zCard(followingListRedisKey);
        //返参
        List<FindFollowingUserRspVO> findFollowingUserRspVOS=null;

        //每页展示10条
        long limit=10;

        if(total>0){
            //缓存中有数据
            //计算一共几页
            long totalPage=PageResponse.getTotalPage(total, limit);

            //如果请求的页码超出了页码总数
            if(pageNo>totalPage)
                return PageResponse.success(null, pageNo, total);

            //准备从Redis中查询ZSet分页数据
            //每页10个，计算偏移量
            long offset=PageResponse.getOffset(pageNo, limit);

            //使用ZREVRANGEBYSCORE命令按score降序获取元素，同时使用limit子句实现分页
            Set<Object> followingUserIdsSet=redisTemplate.opsForZSet().
                    reverseRangeByScore(followingListRedisKey, Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,offset, limit);

            if(CollUtil.isNotEmpty(followingUserIdsSet)){
                //提取所有用户ID到集合中
                List<Long> userIds=followingUserIdsSet.stream().map(object->Long.valueOf(object.toString())).toList();

                //RPC:批量查询用户信息
                findFollowingUserRspVOS=rpcUserServiceAndDTO2VO(userIds,findFollowingUserRspVOS);
            }
        }else{
            //若 Redis 中没有数据，则从数据库查询
            //先查询记录总量
            long count =followingDOMapper.selectCountByUserId(userId);
            //计算一共多少页
            long totalPage=PageResponse.getTotalPage(count, limit);
            //请求的页码超出总页码
            if(pageNo>totalPage)
                return PageResponse.success(null, pageNo, total);
            //偏移量
            long offset=PageResponse.getOffset(pageNo, limit);
            //查询分页数据
            List<FollowingDO> followingDOS=followingDOMapper.selectPageListByUserId(userId,offset,limit);
            //赋值真实的记录总数
            total=count;

            //若记录不为空
            if(CollUtil.isNotEmpty(followingDOS)){
                //提取所有关注用户ID到集合中
                List<Long> userIds=followingDOS.stream().map(FollowingDO::getFollowingUserId).toList();
                //RPC:调用用户服务，并将DTO转换为VO
                findFollowingUserRspVOS=rpcUserServiceAndDTO2VO(userIds,findFollowingUserRspVOS);
                //异步将关注列表全量同步到 Redis
                threadPoolTaskExecutor.submit(() -> syncFollowingList2Redis(userId));
            }
        }
        return PageResponse.success(findFollowingUserRspVOS, pageNo, total);
    }

    //RPC：调用用户服务,将DTO转为VO
    public List<FindFollowingUserRspVO> rpcUserServiceAndDTO2VO(List<Long> userIds, List<FindFollowingUserRspVO> findFollowingUserRspVOS){
        //RPC:批量查询用户信息
        List<FindUserByIdRspDTO> findUserByIdRspDTOS=userRpcService.findByIds(userIds);

        // 若不为空，DTO 转 VO
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            findFollowingUserRspVOS = findUserByIdRspDTOS.stream()
                    .map(dto -> FindFollowingUserRspVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickName(dto.getNickname())
                            .introduction(dto.getIntroduction())
                            .build())
                    .toList();
        }
        return findFollowingUserRspVOS;
    }

    //全量同步关注列表至Redis中
    private void syncFollowingList2Redis(Long userId){
        // 查询全量关注用户列表（1000位用户）
        List<FollowingDO> followingDOS = followingDOMapper.selectAllByUserId(userId);
        if (CollUtil.isNotEmpty(followingDOS)) {
            // 用户关注列表 Redis Key
            String followingListRedisKey = RedisKeyConstants.buildUserFollowingKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildLuaArgs(followingDOS, expireSeconds);
            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(followingListRedisKey), luaArgs);
        }
    }

    //查询粉丝列表
    @Override
    public PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO) {
        //想要查询的用户ID
        Long userId=findFansListReqVO.getUserId();
        //页码
        Integer pageNo=findFansListReqVO.getPageNo();
        //先从Redis中查询
        String fansListRedisKey=RedisKeyConstants.buildUserFansKey(userId);
        //查询目标用户粉丝列表Zset总大小
        long total=redisTemplate.opsForZSet().zCard(fansListRedisKey);
        //返参
        List<FindFansUserRspVO> findFansUserRspVOS=null;
        //每页展示10条
        long limit=10;

        if(total>0){
            //若缓存中有数据
            //计算一共多少页
            long totalPage=PageResponse.getTotalPage(total, limit);
            //请求的页码超出总页码
            if(pageNo>totalPage)
                return PageResponse.success(null, pageNo, total);
            //准备从Redis中查询ZSet分页数据
            //每页10个，计算偏移量
            long offset=PageResponse.getOffset(pageNo, limit);

            //使用ZREVRANGEBYSCORE命令按score降序获取分页数据
            Set<Object> followingUserIdsSet=redisTemplate.opsForZSet()
                    .reverseRangeByScore(fansListRedisKey,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY, offset, limit);

            if(CollUtil.isNotEmpty(followingUserIdsSet)){
                // 提取所有用户 ID 到集合中
                List<Long> userIds = followingUserIdsSet.stream().map(object -> Long.valueOf(object.toString())).toList();

                // RPC: 批量查询用户信息
                findFansUserRspVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRspVOS);
            }
        }else {
            // 若 Redis 缓存中无数据，则查询数据库
            // 先查询记录总量
            total = fansDOMapper.selectCountByUserId(userId);

            // 计算一共多少页
            long totalPage = PageResponse.getTotalPage(total, limit);

            // 请求的页码超出了总页数（只允许查询前 500 页）
            if (pageNo > 500 || pageNo > totalPage) return PageResponse.success(null, pageNo, total);

            // 偏移量
            long offset = PageResponse.getOffset(pageNo, limit);

            // 分页查询
            List<FansDO> fansDOS = fansDOMapper.selectPageListByUserId(userId, offset, limit);

            // 若记录不为空
            if (CollUtil.isNotEmpty(fansDOS)) {
                // 提取所有粉丝用户 ID 到集合中
                List<Long> userIds = fansDOS.stream().map(FansDO::getFansUserId).toList();

                // RPC: 调用用户服务、计数服务，并将 DTO 转换为 VO
                findFansUserRspVOS = rpcUserServiceAndCountServiceAndDTO2VO(userIds, findFansUserRspVOS);

                // 异步将粉丝列表同步到 Redis（最多5000条）
                threadPoolTaskExecutor.submit(() -> syncFansList2Redis(userId));
            }
        }
        return PageResponse.success(findFansUserRspVOS, pageNo, total);
    }

    //粉丝列表同步到Redis 最多5000条
    private void syncFansList2Redis(Long userId){
        List<FansDO> fansDOS = fansDOMapper.select5000FansByUserId(userId);
        if (CollUtil.isNotEmpty(fansDOS)) {
            // 用户粉丝列表 Redis Key
            String fansListRedisKey = RedisKeyConstants.buildUserFansKey(userId);
            // 随机过期时间
            // 保底1天+随机秒数
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            // 构建 Lua 参数
            Object[] luaArgs = buildFansZSetLuaArgs(fansDOS, expireSeconds);

            // 执行 Lua 脚本，批量同步关注关系数据到 Redis 中
            DefaultRedisScript<Long> script = new DefaultRedisScript<>();
            script.setScriptSource(new ResourceScriptSource(new ClassPathResource("/lua/follow_batch_add_and_expire.lua")));
            script.setResultType(Long.class);
            redisTemplate.execute(script, Collections.singletonList(fansListRedisKey), luaArgs);
        }
    }

    //RPC: 调用用户服务，计数服务，将DTO转化为VO粉丝列表
    private List<FindFansUserRspVO> rpcUserServiceAndCountServiceAndDTO2VO(List<Long> userIds, List<FindFansUserRspVO> findFansUserRspVOS){
        //RPC: 批量查询用户信息
        List<FindUserByIdRspDTO> findUserByIdRspDTOS = userRpcService.findByIds(userIds);

        //若不为空， DTO转VO
        if(CollUtil.isNotEmpty(findUserByIdRspDTOS)){
            findFansUserRspVOS=findUserByIdRspDTOS.stream()
                    .map(dto -> FindFansUserRspVO.builder()
                            .userId(dto.getId())
                            .avatar(dto.getAvatar())
                            .nickname(dto.getNickname())
                            .noteTotal(0L)
                            .fansTotal(0L)
                            .build())
                    .toList();
        }
        return findFansUserRspVOS;
    }

    //构建Lua脚本参数：粉丝列表
    private static Object[] buildFansZSetLuaArgs(List<FansDO> fansDOS, long expireSeconds) {
        // 每个粉丝关系有 2 个参数（score 和 value），再加一个过期时间
        int argsLength = fansDOS.size() * 2 + 1;
        Object[] luaArgs = new Object[argsLength];
        int i = 0;
        for (FansDO fansDO : fansDOS) {
            // 粉丝的关注时间作为 score
            luaArgs[i] = DateUtils.localDateTime2Timestamp(fansDO.getCreateTime());
            // 粉丝的用户 ID 作为 ZSet value
            luaArgs[i + 1] = fansDO.getFansUserId();
            i += 2;
        }
        // 最后一个参数是 ZSet 的过期时间
        luaArgs[argsLength - 1] = expireSeconds;
        return luaArgs;
    }
}

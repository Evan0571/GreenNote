package com.evan.greennote.user.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.evan.framework.biz.context.holder.LoginUserContextHolder;
import com.evan.framework.common.enums.DeleteEnum;
import com.evan.framework.common.enums.StatusEnum;
import com.evan.framework.common.exception.BizException;
import com.evan.framework.common.response.Response;
import com.evan.framework.common.util.DateUtils;
import com.evan.framework.common.util.JsonUtils;
import com.evan.framework.common.util.NumberUtils;
import com.evan.greennote.count.dto.FindUserCountsByIdRspDTO;
import com.evan.greennote.search.api.FileFeignApi;
import com.evan.greennote.user.biz.constant.MQConstants;
import com.evan.greennote.user.biz.constant.RedisKeyConstants;
import com.evan.greennote.user.biz.constant.RoleConstants;
import com.evan.greennote.user.biz.domain.dataobject.RoleDO;
import com.evan.greennote.user.biz.domain.dataobject.UserDO;
import com.evan.greennote.user.biz.domain.dataobject.UserRoleDO;
import com.evan.greennote.user.biz.domain.mapper.RoleDOMapper;
import com.evan.greennote.user.biz.domain.mapper.UserDOMapper;
import com.evan.greennote.user.biz.domain.mapper.UserRoleDOMapper;
import com.evan.greennote.user.biz.enums.ResponseCodeEnum;
import com.evan.greennote.user.biz.enums.SexEnum;
import com.evan.greennote.user.biz.model.vo.FindUserProfileReqVO;
import com.evan.greennote.user.biz.model.vo.FindUserProfileRspVO;
import com.evan.greennote.user.biz.model.vo.UpdateUserInfoReqVO;
import com.evan.greennote.user.biz.rpc.CountRpcService;
import com.evan.greennote.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.evan.greennote.user.biz.rpc.OssRpcService;
import com.evan.greennote.user.biz.service.UserService;
import com.evan.greennote.user.biz.util.ParamUtils;
import com.evan.greennote.user.dto.req.*;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import feign.Feign;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//用户业务
@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserDOMapper userDOMapper;
    @Resource
    private FileFeignApi fileFeignApi;
    @Autowired
    private OssRpcService ossRpcService;
    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;
    @Resource
    private UserDOMapper userMapper;
    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    @Resource
    private CountRpcService countRpcService;
    @Resource
    private RocketMQTemplate rocketMQTemplate;

    //用户信息本地缓存
    private static final Cache<Long,FindUserByIdRspDTO> LOCAL_CACHE=Caffeine.newBuilder()
            .initialCapacity(10000)//设置初始容量
            .maximumSize(10000)//设置最大容量
            .expireAfterWrite(1, TimeUnit.HOURS)//缓存条目在写入一小时后过期
            .build();

    //用户主页信息本地缓存
    private static final Cache<Long, FindUserProfileRspVO> PROFILE_LOCAL_CACHE = Caffeine.newBuilder()
            .initialCapacity(10000) // 设置初始容量为 10000 个条目
            .maximumSize(10000) // 设置缓存的最大容量为 10000 个条目
            .expireAfterWrite(5, TimeUnit.MINUTES) // 设置缓存条目在写入后 5 分钟过期
            .build();

    //更新用户信息
    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {

        // 被更新的用户 ID
        Long userId = updateUserInfoReqVO.getUserId();
        // 当前登录的用户 ID
        Long loginUserId = LoginUserContextHolder.getUserId();

        // 非号主本人，无法修改其个人信息
        if (!Objects.equals(loginUserId, userId)) {
            throw new BizException(ResponseCodeEnum.CANT_UPDATE_OTHER_USER_PROFILE);
        }


        UserDO userDO=new UserDO();
        // 设置当前需要更新的用户 ID
        userDO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            String avatar=ossRpcService.uploadFile(avatarFile);
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatar);
            //若上传头像失败
            if(StringUtils.isBlank(avatar)){
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            userDO.setAvatar(avatar);
            needUpdate=true;
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickName(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小绿书号
        String greennoteId = updateUserInfoReqVO.getGreennoteId();
        if (StringUtils.isNotBlank(greennoteId)) {
            Preconditions.checkArgument(ParamUtils.checkGreenNoteId(greennoteId), ResponseCodeEnum.GREENNOTE_ID_VALID_FAIL.getErrorMessage());
            userDO.setGreennoteId(greennoteId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            String backgroundImg=ossRpcService.uploadFile(backgroundImgFile);
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundImgFile);
            //若上传背景图失败
            if(StringUtils.isBlank(backgroundImg)){
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            userDO.setBackgroundImg(backgroundImg);
            needUpdate=true;
        }

        if (needUpdate) {

            // 删除用户缓存
            deleteUserRedisCache(userId);

            // 更新用户信息
            userDO.setUpdateTime(LocalDateTime.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);

            // 延时双删
            sendDelayDeleteUserRedisCacheMQ(userId);

        }
        return Response.success();
    }

    //用户注册
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO){
        String email = registerUserReqDTO.getEmail();
        //判断邮箱是否已被注册
        UserDO userDO1=userDOMapper.selectByEmail(email);
        log.info("==> 用户是否注册，email：{}，userDO：{}",email, JsonUtils.toJsonString(userDO1));
        //若已注册，返回用户ID
        if(Objects.nonNull(userDO1)){
            return Response.success(userDO1.getId());
        }
        //否则注册新用户
        Long userId = distributedIdGeneratorRpcService.getUserId();
        //RPC: 调用分布式 ID 生成服务生成 greennote ID
        String greennoteId=distributedIdGeneratorRpcService.getGreenNoteId();
        UserDO userDO=UserDO.builder()
                .id(userId)
                .email(email)
                .greennoteId(greennoteId)
                .nickname("小绿书"+greennoteId)
                .status(Byte.valueOf(StatusEnum.ENABLE.getValue().toString()))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeleteEnum.NO.getValue())
                .build();
        //添加入库
        userDOMapper.insert(userDO);
        //给该用户分配默认角色
        UserRoleDO userRoleDO=UserRoleDO.builder()
                .userId(userId)
                .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(DeleteEnum.NO.getValue())
                .build();
        userRoleDOMapper.insert(userRoleDO);
        RoleDO roleDO=roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);
        //将该用户的角色ID存入Redis中
        List<String> roles=new ArrayList<>(1);
        roles.add(roleDO.getRoleKey());
        String userRolesKey=RedisKeyConstants.buildUserRolesKey(userId);
        redisTemplate.opsForValue().set(userRolesKey,JsonUtils.toJsonString(roles));
        return Response.success(userId);
    }

    //根据邮箱号查询用户信息
    @Override
    public Response<FindUserByEmailRspDTO> findByEmail(FindUserByEmailReqDTO findUserByEmailReqDTO){
        String email=findUserByEmailReqDTO.getEmail();
        //根据邮箱号查询
        UserDO userDO=userDOMapper.selectByEmail(email);
        //判断用户是否存在
        if(Objects.isNull(userDO)){
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        //构建反参
        FindUserByEmailRspDTO  findUserByEmailRspDTO=FindUserByEmailRspDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();
        return Response.success(findUserByEmailRspDTO);
    }
    //更新密码
    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO){
        //获取当前请求对应的用户ID
        Long userId=LoginUserContextHolder.getUserId();
        UserDO userDO=UserDO.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword())
                .updateTime(LocalDateTime.now())
                .build();
        //更新密码
        userDOMapper.updateByPrimaryKeySelective(userDO);
        return Response.success();
    }

    //根据用户ID查询用户信息
    @Override
    public Response<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        Long userId=findUserByIdReqDTO.getId();

        //先从本地缓存中查询
        FindUserByIdRspDTO findUserByIdRspDTOLocalCache=LOCAL_CACHE.getIfPresent(userId);
        if(Objects.nonNull(findUserByIdRspDTOLocalCache)){
            log.info("==> 命中了本地缓存：{}",findUserByIdRspDTOLocalCache);
            return Response.success(findUserByIdRspDTOLocalCache);
        }

        //用户缓存Redis Key
        String userInfoRedisKey=RedisKeyConstants.buildUserInfoKey(userId);
        //先从Redis缓存中查询
        String userInfoRedisValue=(String) redisTemplate.opsForValue().get(userInfoRedisKey);
        //若Redis缓存中存在该用户信息
        if(StringUtils.isNotBlank(userInfoRedisValue)){
            //将储存的Json字符串转换成对象并返回
            FindUserByIdRspDTO findUserByIdRspDTO=JsonUtils.parseObject(userInfoRedisValue,FindUserByIdRspDTO.class);
            threadPoolTaskExecutor.execute(()->{
                if(Objects.nonNull(findUserByIdRspDTO)){
                    //写入本地缓存
                    LOCAL_CACHE.put(userId,findUserByIdRspDTO);
                }
            });
            return Response.success(findUserByIdRspDTO);
        }
        //若没找到，从数据库查询
        //根据用户ID查询用户信息
        UserDO userDO=userDOMapper.selectByPrimaryKey(userId);
        //判空
        if(Objects.isNull(userDO)){
            threadPoolTaskExecutor.execute(()->{
                //防止缓存穿透，将空数据存入redis缓存，并设置过期时长(1min+随机秒数)
                long expireSeconds=60+ RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(userInfoRedisKey,"null",expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }
        //构建反参
        FindUserByIdRspDTO findUserByIdRspDTO=FindUserByIdRspDTO.builder()
                .id(userDO.getId())
                .nickname(userDO.getNickname())
                .avatar(userDO.getAvatar())
                .introduction(userDO.getIntroduction())
                .build();

        //异步将用户信息存入redis缓存，提示响应速度
        threadPoolTaskExecutor.execute(()->{
            //设置过期时间（1天+随机秒数）
            // 分散过期时间，防止大量缓存同时失效，导数数据库压力过大
            long expireSeconds=60*60*24+ RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue().set(userInfoRedisKey,JsonUtils.toJsonString(findUserByIdRspDTO),expireSeconds, TimeUnit.SECONDS);
        });
        return Response.success(findUserByIdRspDTO);
    }

    //批量根据用户 ID 查询用户信息
    @Override
    public Response<List<FindUserByIdRspDTO>> findByIds(FindUserByIdsReqDTO findUserByIdsReqDTO) {
        //需要查询的用户 ID 集合
        List<Long> userIds=findUserByIdsReqDTO.getIds();
        //构建 Redis Key集合
        List<String> redisKeys=userIds.stream()
                .map(RedisKeyConstants::buildUserInfoKey)
                .toList();
        //先从 Redis 缓存中查， multiGet 批量查询提升性能
        List<Object> redisValues=redisTemplate.opsForValue().multiGet(redisKeys);
        //如果缓存中不为空
        if(CollUtil.isNotEmpty(redisValues)){
            //过滤掉为空的数据
            redisValues=redisValues.stream()
                    .filter(Objects::nonNull)
                    .toList();
        }
        //返参
        List<FindUserByIdRspDTO> findUserByIdRspDTOS= Lists.newArrayList();
        //将过滤后的缓存集合，转换为 DTO 返参实体类
        if(CollUtil.isNotEmpty(redisValues)){
            findUserByIdRspDTOS=redisValues.stream()
                    .map(value -> JsonUtils.parseObject(String.valueOf(value), FindUserByIdRspDTO.class))
                    .collect(Collectors.toList());
        }
        //如果被查询的用户信息都在 Redis 缓存中，则直接返回
        if(CollUtil.size(userIds)==CollUtil.size(findUserByIdRspDTOS)){
            return Response.success(findUserByIdRspDTOS);
        }

        //另外两种情况：1.缓存中没有，2.缓存中不全，要从数据库补充
        //筛选缓存中没有的数据，去数据库查询
        List<Long> userIdsNeedQuery=null;
        if(CollUtil.isNotEmpty(findUserByIdRspDTOS)){
            //将 findUserInfoByIdRspDTOS 集合转 Map
            Map<Long, FindUserByIdRspDTO> map=findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));
            //筛选需要查询DB的用户ID
            userIdsNeedQuery=userIds.stream()
                    .filter(id->Objects.isNull(map.get(id)))
                    .toList();
        }else{
            //若缓存中一条用户信息都没查到，则提交的用户 ID 集合都需要查数据库
            userIdsNeedQuery=userIds;
        }
        //从数据库批量查询
        List<UserDO> userDOS=userDOMapper.selectByIds(userIdsNeedQuery);
        List<FindUserByIdRspDTO> findUserByIdRspDTOS2=null;

        //若数据库查询的记录不为空
        if (CollUtil.isNotEmpty(userDOS)){
            //DO 转 DTO
            findUserByIdRspDTOS2=userDOS.stream()
                    .map(userDO -> FindUserByIdRspDTO.builder()
                            .id(userDO.getId())
                            .nickname(userDO.getNickname())
                            .avatar(userDO.getAvatar())
                            .introduction(userDO.getIntroduction())
                            .build())
                    .collect(Collectors.toList());
            //异步线程将用户信息同步到 Redis 中
            List<FindUserByIdRspDTO> finalFindUserByIdRspDTOS = findUserByIdRspDTOS2;
            threadPoolTaskExecutor.execute(()->{
                //DTO 集合转 Map
                Map<Long, FindUserByIdRspDTO> map=finalFindUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRspDTO::getId, p -> p));
                //执行pipeline操作
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations){
                        for(UserDO userDO:userDOS){
                            Long userId=userDO.getId();
                            //用户信息缓存Redis Key
                            String userInfoRedisKey=RedisKeyConstants.buildUserInfoKey(userId);
                            //DTO转JSON字符串
                            FindUserByIdRspDTO findUserByIdRspDTO=map.get(userId);
                            String value=JsonUtils.toJsonString(findUserByIdRspDTO);
                            //过期时间，保底一天加随机秒
                            long expireSeconds=60*60*24+RandomUtil.randomInt(60*60*24);
                            operations.opsForValue().set(userInfoRedisKey,value,expireSeconds,TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });
            });
        }
        //合并数据
        if(CollUtil.isNotEmpty(findUserByIdRspDTOS2)){
            findUserByIdRspDTOS.addAll(findUserByIdRspDTOS2);
        }
        return Response.success(findUserByIdRspDTOS);
    }

    //获取用户主页信息
    @Override
    public Response<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO) {
        // 要查询的用户 ID
        Long userId = findUserProfileReqVO.getUserId();

        // 若入参中用户 ID 为空，则查询当前登录用户
        if (Objects.isNull(userId)) {
            userId = LoginUserContextHolder.getUserId();
        }

        // 1. 优先查本地缓存
        if (!Objects.equals(userId, LoginUserContextHolder.getUserId())) { // 如果是用户本人查看自己的主页，则不走本地缓存（对本人保证实时性）
            FindUserProfileRspVO userProfileLocalCache = PROFILE_LOCAL_CACHE.getIfPresent(userId);
            if (Objects.nonNull(userProfileLocalCache)) {
                log.info("## 用户主页信息命中本地缓存: {}", JsonUtils.toJsonString(userProfileLocalCache));
                return Response.success(userProfileLocalCache);
            }
        }

        // 2. 再查询 Redis 缓存
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);

        String userProfileJson = (String) redisTemplate.opsForValue().get(userProfileRedisKey);

        if (StringUtils.isNotBlank(userProfileJson)) {
            FindUserProfileRspVO findUserProfileRspVO = JsonUtils.parseObject(userProfileJson, FindUserProfileRspVO.class);
            // 异步同步到本地缓存
            syncUserProfile2LocalCache(userId, findUserProfileRspVO);
            // 如果是博主本人查看，保证计数的实时性
            authorGetActualCountData(userId, findUserProfileRspVO);

            return Response.success(findUserProfileRspVO);
        }

        // 3. 再查询数据库
        UserDO userDO = userMapper.selectByPrimaryKey(userId);

        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参 VO
        FindUserProfileRspVO findUserProfileRspVO = FindUserProfileRspVO.builder()
                .userId(userDO.getId())
                .avatar(userDO.getAvatar())
                .nickname(userDO.getNickname())
                .greennoteId(userDO.getGreennoteId())
                .sex(userDO.getSex())
                .introduction(userDO.getIntroduction())
                .build();

        // 计算年龄
        LocalDate birthday = userDO.getBirthday();
        findUserProfileRspVO.setAge(Objects.isNull(birthday) ? 0 : DateUtils.calculateAge(birthday));

        // RPC: Feign 调用计数服务
        // 关注数、粉丝数、收藏与点赞总数；获得的点赞数、收藏数
        rpcCountServiceAndSetData(userId, findUserProfileRspVO);
        FindUserCountsByIdRspDTO findUserCountsByIdRspDTO = countRpcService.findUserCountById(userId);

        if (Objects.nonNull(findUserCountsByIdRspDTO)) {
            Long fansTotal = findUserCountsByIdRspDTO.getFansTotal();
            Long followingTotal = findUserCountsByIdRspDTO.getFollowingTotal();
            Long likeTotal = findUserCountsByIdRspDTO.getLikeTotal();
            Long collectTotal = findUserCountsByIdRspDTO.getCollectTotal();
            Long noteTotal = findUserCountsByIdRspDTO.getNoteTotal();

            findUserProfileRspVO.setFansTotal(NumberUtils.formatNumberString(fansTotal));
            findUserProfileRspVO.setFollowingTotal(NumberUtils.formatNumberString(followingTotal));
            findUserProfileRspVO.setLikeAndCollectTotal(NumberUtils.formatNumberString(likeTotal + collectTotal));
            findUserProfileRspVO.setNoteTotal(NumberUtils.formatNumberString(noteTotal));
            findUserProfileRspVO.setLikeTotal(NumberUtils.formatNumberString(likeTotal));
            findUserProfileRspVO.setCollectTotal(NumberUtils.formatNumberString(collectTotal));
        }

        // 异步同步到 Redis 中
        syncUserProfile2Redis(userProfileRedisKey, findUserProfileRspVO);
        // 异步同步到本地缓存
        syncUserProfile2LocalCache(userId, findUserProfileRspVO);

        return Response.success(findUserProfileRspVO);
    }

    //异步同步到 Redis 中
    private void syncUserProfile2Redis(String userProfileRedisKey, FindUserProfileRspVO findUserProfileRspVO) {
        threadPoolTaskExecutor.submit(() -> {
            // 设置随机过期时间 (2小时以内)
            long expireTime = 60*60 + RandomUtil.randomInt(60 * 60);

            // 将 VO 转为 Json 字符串写入到 Redis 中
            redisTemplate.opsForValue().set(userProfileRedisKey, JsonUtils.toJsonString(findUserProfileRspVO), expireTime, TimeUnit.SECONDS);
        });
    }

    //异步同步到本地缓存
    private void syncUserProfile2LocalCache(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        threadPoolTaskExecutor.submit(() -> {
            PROFILE_LOCAL_CACHE.put(userId, findUserProfileRspVO);
        });
    }

    //删除 Redis 中的用户缓存
    private void deleteUserRedisCache(Long userId) {
        // 构建 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);
        String userProfileRedisKey = RedisKeyConstants.buildUserProfileKey(userId);

        // 批量删除
        redisTemplate.delete(Arrays.asList(userInfoRedisKey, userProfileRedisKey));
    }

    //异步发送延时消息
    private void sendDelayDeleteUserRedisCacheMQ(Long userId) {
        Message<String> message = MessageBuilder.withPayload(String.valueOf(userId))
                .build();

        rocketMQTemplate.asyncSend(MQConstants.TOPIC_DELAY_DELETE_USER_REDIS_CACHE, message,
                new SendCallback() {
                    @Override
                    public void onSuccess(SendResult sendResult) {
                        log.info("## 延时删除 Redis 用户缓存消息发送成功...");
                    }

                    @Override
                    public void onException(Throwable e) {
                        log.error("## 延时删除 Redis 用户缓存消息发送失败...", e);
                    }
                },
                3000, // 超时时间
                1 // 延迟级别，1 表示延时 1s
        );
    }

    //Feign 调用计数服务, 并设置计数数据
    private void rpcCountServiceAndSetData(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        FindUserCountsByIdRspDTO findUserCountsByIdRspDTO = countRpcService.findUserCountById(userId);

        if (Objects.nonNull(findUserCountsByIdRspDTO)) {
            Long fansTotal = findUserCountsByIdRspDTO.getFansTotal();
            Long followingTotal = findUserCountsByIdRspDTO.getFollowingTotal();
            Long likeTotal = findUserCountsByIdRspDTO.getLikeTotal();
            Long collectTotal = findUserCountsByIdRspDTO.getCollectTotal();
            Long noteTotal = findUserCountsByIdRspDTO.getNoteTotal();

            findUserProfileRspVO.setFansTotal(NumberUtils.formatNumberString(fansTotal));
            findUserProfileRspVO.setFollowingTotal(NumberUtils.formatNumberString(followingTotal));
            findUserProfileRspVO.setLikeAndCollectTotal(NumberUtils.formatNumberString(likeTotal + collectTotal));
            findUserProfileRspVO.setNoteTotal(NumberUtils.formatNumberString(noteTotal));
            findUserProfileRspVO.setLikeTotal(NumberUtils.formatNumberString(likeTotal));
            findUserProfileRspVO.setCollectTotal(NumberUtils.formatNumberString(collectTotal));
        }
    }

    //作者本人获取真实的计数数据（保证实时性）
    private void authorGetActualCountData(Long userId, FindUserProfileRspVO findUserProfileRspVO) {
        if (Objects.equals(userId, LoginUserContextHolder.getUserId())) { // 如果是博主本人
            rpcCountServiceAndSetData(userId, findUserProfileRspVO);
        }
    }
}

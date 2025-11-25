package com.evan.greennote.auth.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.evan.framework.common.exception.BizException;
import com.evan.framework.common.response.Response;
import com.evan.greennote.auth.constant.RedisKeyConstants;
import com.evan.greennote.auth.enums.LoginTypeEnum;
import com.evan.greennote.auth.enums.ResponseCodeEnum;
import com.evan.greennote.auth.filter.LoginUserContextHolder;
import com.evan.greennote.auth.model.vo.user.UpdatePasswordReqVO;
import com.evan.greennote.auth.model.vo.user.UserLoginReqVO;
import com.evan.greennote.auth.rpc.UserRpcService;
import com.evan.greennote.auth.service.AuthService;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import com.google.common.base.Preconditions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRpcService userRpcService;

    //用户登录注册（验证码/密码）
    @Override
    public Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO){
        String email = userLoginReqVO.getEmail();
        Integer type=userLoginReqVO.getType();

        LoginTypeEnum loginTypeEnum=LoginTypeEnum.valueOf(type);

        //登陆类型错误
        if(Objects.isNull(loginTypeEnum)){
            throw new BizException(ResponseCodeEnum.LOGIN_TYPE_ERROR);
        }

        Long userId=null;

        //登陆类型判断
        switch (loginTypeEnum){
            //邮箱验证码登录
            case VERIFICATION_CODE:
                log.info("开始验证码登录流程, email: {}", email);
                String verificationCode=userLoginReqVO.getCode();
                //校验入参验证码是否为空
                Preconditions.checkArgument(StringUtils.isNotBlank(verificationCode),"验证码不能为空");
                //构建验证码redis key
                String key= RedisKeyConstants.buildVerificationCodeKey(email);
                //查询存在redis中该用户的验证码
                log.info("验证码校验结果, redis中的验证码: {}, 用户输入: {}", key, verificationCode);
                String sentCode=(String)redisTemplate.opsForValue().get(key);
                //判断用户提交的验证码与redis中储存的是否一致
                if(!StringUtils.equals(verificationCode,sentCode)){
                    throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_ERROR);
                }

                //RPC：调用用户服务，注册用户
                // 先查询用户是否存在
                FindUserByEmailRspDTO existingUser = userRpcService.findUserByEmail(email);
                log.info("用户查询结果: {}", existingUser);
                if (existingUser != null) {
                    // 用户已存在，直接使用该用户ID登录
                    userId = existingUser.getId();
                } else {
                    // 用户不存在，注册新用户
                    Long userIdTmp = userRpcService.registerUser(email);
                    log.info("用户注册结果: {}", userIdTmp);
                    if (Objects.isNull(userIdTmp)) {
                        throw new BizException(ResponseCodeEnum.LOGIN_FAIL);
                    }
                    userId = userIdTmp;
                }
                break;

                //密码登录
            case PASSWORD:
                String password=userLoginReqVO.getPassword();

                //RPC: 根据邮箱号查询
                FindUserByEmailRspDTO findUserByEmailRspDTO=userRpcService.findUserByEmail(email);

                //判断邮箱是否已注册
                if(Objects.isNull(findUserByEmailRspDTO)){
                    throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
                }
                //拿到密文密码
                String encodePassword=findUserByEmailRspDTO.getPassword();
                //匹配密码判断是否一致
                boolean isPasswordCorrect=passwordEncoder.matches(password,encodePassword);
                //如果不正确，抛出业务异常
                if(!isPasswordCorrect){
                    throw new BizException(ResponseCodeEnum.EMAIL_OR_PASSWORD_ERROR);
                }

                userId=findUserByEmailRspDTO.getId();
                break;

            default:
                break;
        }
        // SaToken 登录用户, 入参为用户 ID
        StpUtil.login(userId);
        // 获取 Token 令牌
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        // 返回 Token 令牌
        return Response.success(tokenInfo.tokenValue);
    }

    //用户退出登录
    @Override
    public Response<?> logout() {
        // 检查用户是否已登录
        if (StpUtil.isLogin()) {
            // 获取当前登录用户的ID
            Object loginId = StpUtil.getLoginId();
            log.info("==> 用户退出登录， userId:{}", loginId);
            // 当前用户退出登录
            StpUtil.logout();
        } else {
            log.info("==> 未登录用户尝试退出登录");
        }
        return Response.success();
    }

    //用户更改密码
    @Override
    public Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO){
        Long userId= LoginUserContextHolder.getUserId();
        //新密码
        String newPassword=updatePasswordReqVO.getNewPassword();
        //密码加密
        String encodePassword=passwordEncoder.encode(newPassword);
        //RPC: 调用用户服务：更新密码
        userRpcService.updatePassword(encodePassword);
        log.info("用户 {} 更新密码为：{} 密文为：{}", userId, newPassword,encodePassword);
        return Response.success();
    }
}

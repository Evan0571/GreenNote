package com.evan.greennote.auth.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.evan.framework.common.exception.BizException;
import com.evan.framework.common.response.Response;
import com.evan.greennote.auth.constant.RedisKeyConstants;
import com.evan.greennote.auth.enums.ResponseCodeEnum;
import com.evan.greennote.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.evan.greennote.auth.service.VerificationCodeService;
import com.evan.greennote.auth.util.MailHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
@Slf4j
public class VerificationCodeServiceImpl implements VerificationCodeService {
    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private MailHelper mailHelper;

    @Resource(name="taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private static final Pattern EMAIL_PATTERN=
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public Response<?> send(SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        String email = sendVerificationCodeReqVO.getEmail();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BizException(ResponseCodeEnum.PARAM_NOT_VALID);
        }
        String key= RedisKeyConstants.buildVerificationCodeKey(email);
        boolean isSent=redisTemplate.hasKey(key);
        if(isSent){
            throw new BizException(ResponseCodeEnum.VERIFICATION_CODE_SEND_FREQUENTLY);
        }
        String verificationCode= RandomUtil.randomNumbers(6);
        log.info("==> 邮箱号: {}, 已发送验证码: {}",email,verificationCode);
        redisTemplate.opsForValue().set(key,verificationCode,3,TimeUnit.MINUTES);

        threadPoolTaskExecutor.submit(() -> {
            String subject = "【GreenNote】登录验证码";
            String content = String.format("您的验证码为：%s，3分钟内有效。如非本人操作，请忽略此邮件。", verificationCode);
            boolean ok = mailHelper.sendText(email, subject, content);
            if (!ok) {
                log.warn("==> 邮件发送失败，email={}, 若需要可在此删除Redis验证码或记录重试", email);
            }
        });
        return Response.success();
    }
}

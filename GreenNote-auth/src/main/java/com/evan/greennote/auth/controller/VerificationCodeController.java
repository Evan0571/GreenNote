package com.evan.greennote.auth.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.Response;
import com.evan.greennote.auth.model.vo.verificationcode.SendVerificationCodeReqVO;
import com.evan.greennote.auth.service.VerificationCodeService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class VerificationCodeController {

    @Resource
    private VerificationCodeService verificationCodeService;

    @PostMapping("/verification/code/send")
    @ApiOperationLog(description = "发送邮箱验证码")
    public Response<?> send(@Validated @RequestBody SendVerificationCodeReqVO sendVerificationCodeReqVO) {
        return verificationCodeService.send(sendVerificationCodeReqVO);
    }
}
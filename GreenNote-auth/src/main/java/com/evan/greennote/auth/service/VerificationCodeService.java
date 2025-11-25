package com.evan.greennote.auth.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.auth.model.vo.verificationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {
    Response<?>send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}

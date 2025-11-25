package com.evan.greennote.auth.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.auth.model.vo.user.UpdatePasswordReqVO;
import com.evan.greennote.auth.model.vo.user.UserLoginReqVO;

public interface AuthService {

    //用户登录注册
    Response<String> loginAndRegister(UserLoginReqVO userLoginVO);

    //用户登出
    Response<?> logout();

    //修改密码
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);

}

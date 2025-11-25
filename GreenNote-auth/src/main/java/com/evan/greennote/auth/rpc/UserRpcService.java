package com.evan.greennote.auth.rpc;

import com.evan.framework.common.response.Response;
import com.evan.greennote.user.api.UserFeignApi;
import com.evan.greennote.user.dto.req.FindUserByEmailReqDTO;
import com.evan.greennote.user.dto.req.RegisterUserReqDTO;
import com.evan.greennote.user.dto.req.UpdateUserPasswordReqDTO;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserRpcService {
    @Resource
    private UserFeignApi userFeignApi;
    //注册用户
    public Long registerUser(String email){
        RegisterUserReqDTO registerUserReqDTO = new RegisterUserReqDTO();
        registerUserReqDTO.setEmail(email);
        Response<Long> response = userFeignApi.registerUser(registerUserReqDTO);
        if(!response.isSuccess()){
            log.warn("用户注册失败, email: {}, 错误码: {}, 错误信息: {}",
                    email, response.getErrorCode(), response.getMessage());
            return null;
        }
        return response.getData();
    }

    //根据邮箱查询用户信息
    public FindUserByEmailRspDTO findUserByEmail(String email){
        FindUserByEmailReqDTO findUserByEmailReqDTO = new FindUserByEmailReqDTO();
        findUserByEmailReqDTO.setEmail(email);
        Response<FindUserByEmailRspDTO> response = userFeignApi.findByEmail(findUserByEmailReqDTO);
        if(!response.isSuccess()){
            return null;
        }
        return response.getData();
    }

    //密码更新
    public void updatePassword(String encodePassword){
        UpdateUserPasswordReqDTO updateUserPasswordReqDTO = new UpdateUserPasswordReqDTO();
        updateUserPasswordReqDTO.setEncodePassword(encodePassword);
        userFeignApi.updatePassword(updateUserPasswordReqDTO);
    }
}

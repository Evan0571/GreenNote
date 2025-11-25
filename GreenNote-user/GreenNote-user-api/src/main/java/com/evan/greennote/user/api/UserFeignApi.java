package com.evan.greennote.user.api;

import com.evan.framework.common.response.Response;
import com.evan.greennote.user.constant.ApiConstants;
import com.evan.greennote.user.dto.req.*;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface UserFeignApi {
    String PREFIX = "/user";
    //用户注册
    @PostMapping(value = PREFIX + "/register")
    Response<Long> registerUser(@RequestBody RegisterUserReqDTO registerUserReqDTO);

    //通过邮箱查找用户信息
    @PostMapping(value = PREFIX + "/findByEmail")
    Response<FindUserByEmailRspDTO> findByEmail(@RequestBody FindUserByEmailReqDTO findUserByEmailReqDTO);

    //更新密码
    @PostMapping(value = PREFIX + "/password/update")
    Response<?> updatePassword(@RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    //根据用户 ID 查询用户信息
    @PostMapping(value = PREFIX + "/findById")
    Response<FindUserByIdRspDTO> findById(@RequestBody FindUserByIdReqDTO findUserByIdReqDTO);

    //批量查询用户信息
    @PostMapping(value = PREFIX + "/findByIds")
    Response<List<FindUserByIdRspDTO>> findByIds(@RequestBody FindUserByIdsReqDTO findUserByIdsReqDTO);
}

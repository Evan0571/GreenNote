package com.evan.greennote.user.biz.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.Response;
import com.evan.greennote.user.biz.model.vo.FindUserProfileReqVO;
import com.evan.greennote.user.biz.model.vo.FindUserProfileRspVO;
import com.evan.greennote.user.biz.model.vo.UpdateUserInfoReqVO;
import com.evan.greennote.user.biz.service.UserService;
import com.evan.greennote.user.dto.req.*;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    //用户信息修改
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response<?> updateUserInfo(@Validated UpdateUserInfoReqVO updateUserInfoReqVO) {
        return userService.updateUserInfo(updateUserInfoReqVO);
    }

    //用户注册
    @PostMapping("/register")
    @ApiOperationLog(description = "用户注册")
    public Response<Long> register(@Validated @RequestBody RegisterUserReqDTO registerUserReqDTO) {
        return userService.register(registerUserReqDTO);
    }

    //根据邮箱查询用户信息
    @PostMapping("/findByEmail")
    @ApiOperationLog(description = "根据邮箱查询用户信息")
    public Response<FindUserByEmailRspDTO> findByEmail(@Validated @RequestBody FindUserByEmailReqDTO findUserByEmailReqDTO){
        return userService.findByEmail(findUserByEmailReqDTO);
    }

    //密码更新
    @PostMapping("/password/update")
    @ApiOperationLog(description = "密码更新")
    public Response<?> updatePassword(@Validated @RequestBody UpdateUserPasswordReqDTO updateUserPasswordReqDTO){
        return userService.updatePassword(updateUserPasswordReqDTO);
    }

    //根据id查询用户信息
    @PostMapping("/findById")
    @ApiOperationLog(description = "根据id查询用户信息")
    public Response<FindUserByIdRspDTO> findById(@Validated @RequestBody FindUserByIdReqDTO findUserByIdReqDTO){
        return userService.findById(findUserByIdReqDTO);
    }

    //根据id批量查询用户信息
    @PostMapping("/findByIds")
    @ApiOperationLog(description = "根据id批量查询用户信息")
    public Response<List<FindUserByIdRspDTO>> findByIds(@Validated @RequestBody FindUserByIdsReqDTO findUserByIdsReqDTO){
        return userService.findByIds(findUserByIdsReqDTO);
    }

    //获取用户主页信息
    @PostMapping(value = "/profile")
    public Response<FindUserProfileRspVO> findUserProfile(@Validated @RequestBody FindUserProfileReqVO findUserProfileReqVO) {
        return userService.findUserProfile(findUserProfileReqVO);
    }
}

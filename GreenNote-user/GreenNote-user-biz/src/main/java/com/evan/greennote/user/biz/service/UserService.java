package com.evan.greennote.user.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.user.biz.model.vo.FindUserProfileReqVO;
import com.evan.greennote.user.biz.model.vo.FindUserProfileRspVO;
import com.evan.greennote.user.biz.model.vo.UpdateUserInfoReqVO;
import com.evan.greennote.user.dto.req.*;
import com.evan.greennote.user.dto.resp.FindUserByEmailRspDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;

import java.util.List;

//用户业务
public interface UserService {
    //更新用户信息
    Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO);

    //用户注册
    Response<Long> register(RegisterUserReqDTO registerUserReqDTO);

    //根据邮箱号查询用户信息
    Response<FindUserByEmailRspDTO> findByEmail(FindUserByEmailReqDTO findUserByEmailReqDTO);

    //更新密码
    Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO);

    //根据用户 ID 查询用户信息
    Response<FindUserByIdRspDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO);

    //批量根据用户 ID 查询用户信息
    Response<List<FindUserByIdRspDTO>> findByIds(FindUserByIdsReqDTO findUserByIdsReqDTO);

    //获取用户主页信息
    Response<FindUserProfileRspVO> findUserProfile(FindUserProfileReqVO findUserProfileReqVO);
}

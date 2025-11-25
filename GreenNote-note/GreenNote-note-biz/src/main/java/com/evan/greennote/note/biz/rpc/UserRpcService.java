package com.evan.greennote.note.biz.rpc;

import com.evan.framework.common.response.Response;
import com.evan.greennote.user.api.UserFeignApi;
import com.evan.greennote.user.dto.req.FindUserByIdReqDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

//用户服务
@Component
public class UserRpcService {
    @Resource
    private UserFeignApi userFeignApi;
    //根据ID查找用户信息
    public FindUserByIdRspDTO findById(Long userId){
        FindUserByIdReqDTO findUserByIdReqDTO=new FindUserByIdReqDTO();
        findUserByIdReqDTO.setId(userId);
        Response<FindUserByIdRspDTO> response=userFeignApi.findById(findUserByIdReqDTO);
        if(Objects.isNull(response)||!response.isSuccess()){
            return null;
        }
        return response.getData();
    }
}

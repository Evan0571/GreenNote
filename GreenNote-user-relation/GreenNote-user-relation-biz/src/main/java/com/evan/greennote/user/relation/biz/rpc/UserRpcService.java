package com.evan.greennote.user.relation.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.evan.framework.common.response.Response;
import com.evan.greennote.user.api.UserFeignApi;
import com.evan.greennote.user.dto.req.FindUserByIdReqDTO;
import com.evan.greennote.user.dto.req.FindUserByIdsReqDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UserRpcService {
    @Resource
    private UserFeignApi userFeignApi;
    //用户 ID 查询
    public FindUserByIdRspDTO findById(Long userId){
        FindUserByIdReqDTO findUserByIdReqDTO=new FindUserByIdReqDTO();
        findUserByIdReqDTO.setId(userId);

        Response<FindUserByIdRspDTO> response=userFeignApi.findById(findUserByIdReqDTO);

        if(!response.isSuccess()|| Objects.isNull(response.getData())){
            return null;
        }
        return response.getData();
    }
    //用户 ID 批量查询
    public List<FindUserByIdRspDTO> findByIds(List<Long> userIds){
        FindUserByIdsReqDTO findUserByIdsReqDTO=new FindUserByIdsReqDTO();
        findUserByIdsReqDTO.setIds(userIds);
        Response<List<FindUserByIdRspDTO>> response=userFeignApi.findByIds(findUserByIdsReqDTO);
        if(!response.isSuccess()|| Objects.isNull(response.getData())|| CollUtil.isEmpty(response.getData())){
            return null;
        }
        return response.getData();
    }
}

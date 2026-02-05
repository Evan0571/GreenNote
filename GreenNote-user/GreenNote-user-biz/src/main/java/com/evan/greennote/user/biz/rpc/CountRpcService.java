package com.evan.greennote.user.biz.rpc;

import com.evan.framework.common.response.Response;
import com.evan.greennote.count.api.CountFeignApi;
import com.evan.greennote.count.dto.FindUserCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindUserCountsByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

//计数服务
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    //查询用户计数信息
    public FindUserCountsByIdRspDTO findUserCountById(Long userId) {
        FindUserCountsByIdReqDTO findUserCountsByIdReqDTO = new FindUserCountsByIdReqDTO();
        findUserCountsByIdReqDTO.setUserId(userId);

        Response<FindUserCountsByIdRspDTO> response = countFeignApi.findUserCount(findUserCountsByIdReqDTO);

        if (Objects.isNull(response) || !response.isSuccess()) {
            return null;
        }

        return response.getData();
    }

}
package com.evan.greennote.comment.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.evan.framework.common.response.Response;
import com.evan.greennote.user.api.UserFeignApi;
import com.evan.greennote.user.dto.req.FindUserByIdsReqDTO;
import com.evan.greennote.user.dto.resp.FindUserByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

//用户服务
@Component
public class UserRpcService {

    @Resource
    private UserFeignApi userFeignApi;

    //批量查询用户信息
    public List<FindUserByIdRspDTO> findByIds(List<Long> userIds) {
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }

        FindUserByIdsReqDTO findUsersByIdsReqDTO = new FindUserByIdsReqDTO();
        // 去重, 并设置用户 ID 集合
        findUsersByIdsReqDTO.setIds(userIds.stream().distinct().collect(Collectors.toList()));

        Response<List<FindUserByIdRspDTO>> response = userFeignApi.findByIds(findUsersByIdsReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }

}
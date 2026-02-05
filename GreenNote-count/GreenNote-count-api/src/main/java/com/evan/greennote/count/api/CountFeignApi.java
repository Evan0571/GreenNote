package com.evan.greennote.count.api;

import com.evan.framework.common.response.Response;
import com.evan.greennote.count.constant.ApiConstants;
import com.evan.greennote.count.dto.FindUserCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindUserCountsByIdRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//计数服务 Feign 接口
@FeignClient(name = ApiConstants.SERVICE_NAME)
public interface CountFeignApi {

    String PREFIX = "/count";

    //查询用户计数
    @PostMapping(value = PREFIX + "/user/data")
    Response<FindUserCountsByIdRspDTO> findUserCount(@RequestBody FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);

}

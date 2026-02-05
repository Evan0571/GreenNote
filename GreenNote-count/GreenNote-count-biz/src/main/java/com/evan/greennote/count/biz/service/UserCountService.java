package com.evan.greennote.count.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.count.dto.FindUserCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindUserCountsByIdRspDTO;

//用户计数业务
public interface UserCountService {

    //查询用户相关计数
    Response<FindUserCountsByIdRspDTO> findUserCountData(FindUserCountsByIdReqDTO findUserCountsByIdReqDTO);
}

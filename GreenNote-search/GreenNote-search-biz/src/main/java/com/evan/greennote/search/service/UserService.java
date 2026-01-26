package com.evan.greennote.search.service;

import com.evan.framework.common.response.PageResponse;
import com.evan.greennote.search.model.vo.SearchUserReqVO;
import com.evan.greennote.search.model.vo.SearchUserRspVO;

public interface UserService {
    //搜索用户
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);
}

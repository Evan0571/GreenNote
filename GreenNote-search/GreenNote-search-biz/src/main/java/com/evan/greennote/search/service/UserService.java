package com.evan.greennote.search.service;

import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.greennote.search.dto.RebuildUserDocumentReqDTO;
import com.evan.greennote.search.model.vo.SearchUserReqVO;
import com.evan.greennote.search.model.vo.SearchUserRspVO;

public interface UserService {
    //搜索用户
    PageResponse<SearchUserRspVO> searchUser(SearchUserReqVO searchUserReqVO);
    //重建用户文档
    Response<Long> rebuildDocument(RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO);
}

package com.evan.greennote.search.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.greennote.search.dto.RebuildUserDocumentReqDTO;
import com.evan.greennote.search.model.vo.SearchUserReqVO;
import com.evan.greennote.search.model.vo.SearchUserRspVO;
import com.evan.greennote.search.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//搜索用户
@RestController
@RequestMapping("/search")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/user")
    @ApiOperationLog(description = "搜索用户")
    public PageResponse<SearchUserRspVO> searchUser(@RequestBody @Validated SearchUserReqVO searchUserReqVO) {
        return userService.searchUser(searchUserReqVO);
    }

    //对其他服务提供的接口
    @PostMapping("/user/document/rebuild")
    @ApiOperationLog(description = "用户文档重建")
    public Response<Long> rebuildDocument(@Validated @RequestBody RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO) {
        return userService.rebuildDocument(rebuildUserDocumentReqDTO);
    }

}
package com.evan.greennote.user.relation.biz.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.greennote.user.relation.biz.model.vo.*;
import com.evan.greennote.user.relation.biz.service.RelationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//用户关系
@RestController
@RequestMapping("/relation")
@Slf4j
public class RelationController {
    @Resource
    private RelationService relationService;
    //关注
    @PostMapping("/follow")
    @ApiOperationLog(description = "关注用户")
    public Response<?> follow(@Validated @RequestBody FollowUserReqVO followUserReqVO){
        return relationService.follow(followUserReqVO);
    }
    //取关
    @PostMapping("/unfollow")
    @ApiOperationLog(description = "取关用户")
    public Response<?> unfollow(@Validated @RequestBody UnfollowUserReqVO unfollowUserReqVO){
        return relationService.unfollow(unfollowUserReqVO);
    }
    //查询关注列表
    @PostMapping("/following/list")
    @ApiOperationLog(description = "查询关注列表")
    public PageResponse<FindFollowingUserRspVO> findFollowingList(@Validated @RequestBody FindFollowingListReqVO findFollowingListReqVO){
        return relationService.findFollowingList(findFollowingListReqVO);
    }
    //查询粉丝列表
    @PostMapping("/fans/list")
    @ApiOperationLog(description = "查询粉丝列表")
    public PageResponse<FindFansUserRspVO> findFansList(@Validated @RequestBody FindFansListReqVO findFansListReqVO){
        return relationService.findFansList(findFansListReqVO);
    }
}

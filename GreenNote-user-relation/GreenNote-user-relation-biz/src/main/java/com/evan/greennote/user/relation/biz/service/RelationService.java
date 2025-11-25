package com.evan.greennote.user.relation.biz.service;

import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.greennote.user.relation.biz.model.vo.*;

public interface RelationService {
    //关注用户
    Response<?> follow(FollowUserReqVO followUserReqVO);
    //取关用户
    Response<?> unfollow(UnfollowUserReqVO unfollowUserReqVO);
    //查询关注列表
    PageResponse<FindFollowingUserRspVO> findFollowingList(FindFollowingListReqVO findFollowingListReqVO);
    //查询粉丝列表
    PageResponse<FindFansUserRspVO> findFansList(FindFansListReqVO findFansListReqVO);
}

package com.evan.greennote.comment.biz.service;

import com.evan.framework.common.response.PageResponse;
import com.evan.framework.common.response.Response;
import com.evan.greennote.comment.biz.model.vo.*;

//评论业务
public interface CommentService {

    //发布评论
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);

    //评论列表分页查询
    PageResponse<FindCommentItemRspVO> findCommentPageList(FindCommentPageListReqVO findCommentPageListReqVO);

    //二级评论分页查询
    PageResponse<FindChildCommentItemRspVO> findChildCommentPageList(FindChildCommentPageListReqVO findChildCommentPageListReqVO);

    //评论点赞
    Response<?> likeComment(LikeCommentReqVO likeCommentReqVO);

    //取消评论点赞
    Response<?> unlikeComment(UnLikeCommentReqVO unLikeCommentReqVO);

    //删除评论
    Response<?> deleteComment(DeleteCommentReqVO deleteCommentReqVO);

    //删除本地评论缓存
    void deleteCommentLocalCache(Long commentId);

}


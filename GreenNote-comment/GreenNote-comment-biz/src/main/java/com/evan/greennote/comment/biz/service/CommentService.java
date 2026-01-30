package com.evan.greennote.comment.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.comment.biz.model.vo.PublishCommentReqVO;

//评论业务
public interface CommentService {

    //发布评论
    Response<?> publishComment(PublishCommentReqVO publishCommentReqVO);
}


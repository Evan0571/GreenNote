package com.evan.greennote.kv.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.kv.dto.req.BatchAddCommentContentReqDTO;
import com.evan.greennote.kv.dto.req.BatchFindCommentContentReqDTO;
import com.evan.greennote.kv.dto.req.DeleteCommentContentReqDTO;

//评论内容存储业务
public interface CommentContentService {

    //批量添加评论内容
    Response<?> batchAddCommentContent(BatchAddCommentContentReqDTO batchAddCommentContentReqDTO);

    //批量查询评论内容
    Response<?> batchFindCommentContent(BatchFindCommentContentReqDTO batchFindCommentContentReqDTO);

    //删除评论内容
    Response<?> deleteCommentContent(DeleteCommentContentReqDTO deleteCommentContentReqDTO);

}


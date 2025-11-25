package com.evan.greennote.kv.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.kv.dto.req.AddNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.DeleteNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.FindNoteContentReqDTO;
import com.evan.greennote.kv.dto.rsp.FindNoteContentRspDTO;

//笔记内容存储业务
public interface NoteContentService {
    //添加笔记内容
    Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO);

    //查询笔记内容
    Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO);

    //删除笔记内容
    Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}

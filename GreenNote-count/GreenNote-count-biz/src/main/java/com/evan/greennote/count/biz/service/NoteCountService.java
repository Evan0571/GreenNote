package com.evan.greennote.count.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.count.dto.FindNoteCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindNoteCountsByIdRspDTO;

import java.util.List;

//笔记计数业务
public interface NoteCountService {

    //批量查询笔记计数
    Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(FindNoteCountsByIdReqDTO findNoteCountsByIdsReqDTO);
}
package com.evan.greennote.search.service;

import com.evan.framework.common.response.PageResponse;
import com.evan.greennote.search.model.vo.SearchNoteReqVO;
import com.evan.greennote.search.model.vo.SearchNoteRspVO;

//笔记搜索业务
public interface NoteService {
    //搜索笔记
    PageResponse<SearchNoteRspVO> searchNote(SearchNoteReqVO searchNoteReqVO);
}
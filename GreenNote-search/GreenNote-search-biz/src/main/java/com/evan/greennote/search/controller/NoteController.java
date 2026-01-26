package com.evan.greennote.search.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.PageResponse;
import com.evan.greennote.search.model.vo.SearchNoteReqVO;
import com.evan.greennote.search.model.vo.SearchNoteRspVO;
import com.evan.greennote.search.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//笔记搜索
@RestController
@RequestMapping("/search")
@Slf4j
public class NoteController {

    @Resource
    private NoteService noteService;

    @PostMapping("/note")
    @ApiOperationLog(description = "搜索笔记")
    public PageResponse<SearchNoteRspVO> searchNote(@RequestBody @Validated SearchNoteReqVO searchNoteReqVO) {
        return noteService.searchNote(searchNoteReqVO);
    }
}
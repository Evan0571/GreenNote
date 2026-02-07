package com.evan.greennote.count.biz.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.Response;
import com.evan.greennote.count.biz.service.NoteCountService;
import com.evan.greennote.count.dto.FindNoteCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindNoteCountsByIdRspDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

//用户维度计数
@RestController
@RequestMapping("/count")
@Slf4j
public class NoteCountController {

    @Resource
    private NoteCountService noteCountService;

    @PostMapping(value = "/notes/data")
    @ApiOperationLog(description = "批量获取笔记计数数据")
    public Response<List<FindNoteCountsByIdRspDTO>> findNotesCountData(@Validated @RequestBody FindNoteCountsByIdReqDTO findNoteCountsByIdReqDTO) {
        return noteCountService.findNotesCountData(findNoteCountsByIdReqDTO);
    }

}
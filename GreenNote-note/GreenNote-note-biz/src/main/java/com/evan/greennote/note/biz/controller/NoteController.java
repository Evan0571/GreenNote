package com.evan.greennote.note.biz.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.framework.common.response.Response;
import com.evan.greennote.note.biz.model.vo.*;
import com.evan.greennote.note.biz.service.NoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//笔记控制
@RestController
@RequestMapping("/note")
@Slf4j
public class NoteController {
    @Resource
    private NoteService noteService;

    //笔记发布
    @PostMapping(value="/publish")
    @ApiOperationLog(description="笔记发布")
    public Response<?> publishNote(@Validated @RequestBody PublishNoteReqVO publishNoteReqVO){
        return noteService.publishNote(publishNoteReqVO);
    }

    //笔记详情
    @PostMapping(value="/detail")
    @ApiOperationLog(description="笔记详情")
    public Response<FindNoteDetailRspVO> findNoteDetail(@Validated @RequestBody FindNoteDetailReqVO findNoteDetailReqVO){
        return noteService.findNoteDetail(findNoteDetailReqVO);
    }

    //笔记修改
    @PostMapping(value = "/update")
    @ApiOperationLog(description = "笔记修改")
    public Response<?> updateNote(@Validated @RequestBody UpdateNoteReqVO updateNoteReqVO) {
        return noteService.updateNote(updateNoteReqVO);
    }

    //笔记删除
    @PostMapping(value = "/delete")
    @ApiOperationLog(description = "笔记删除")
    public Response<?> deleteNote(@Validated @RequestBody DeleteNoteReqVO deleteNoteReqVO) {
        return noteService.deleteNote(deleteNoteReqVO);
    }

    //笔记仅对自己可见
    @PostMapping(value = "/visible/onlyme")
    @ApiOperationLog(description = "笔记仅对自己可见")
    public Response<?> visibleOnlyMe(@Validated @RequestBody UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO) {
        return noteService.visibleOnlyMe(updateNoteVisibleOnlyMeReqVO);
    }

    //笔记置顶/取消置顶
    @PostMapping(value = "/top")
    @ApiOperationLog(description = "笔记置顶/取消置顶")
    public Response<?> topNote(@Validated @RequestBody TopNoteReqVO topNoteReqVO) {
        return noteService.topNote(topNoteReqVO);
    }

    //笔记点赞
    @PostMapping(value = "/like")
    @ApiOperationLog(description = "笔记点赞")
    public Response<?> likeNote(@Validated @RequestBody LikeNoteReqVO likeNoteReqVO) {
        return noteService.likeNote(likeNoteReqVO);
    }

    //笔记取消点赞
    @PostMapping(value = "/unlike")
    @ApiOperationLog(description = "笔记取消点赞")
    public Response<?> unlikeNote(@Validated @RequestBody UnlikeNoteReqVO unlikeNoteReqVO) {
        return noteService.unlikeNote(unlikeNoteReqVO);
    }

    //笔记收藏
    @PostMapping(value = "/collect")
    @ApiOperationLog(description = "笔记收藏")
    public Response<?> collectNote(@Validated @RequestBody CollectNoteReqVO collectNoteReqVO) {
        return noteService.collectNote(collectNoteReqVO);
    }

    //笔记取消收藏
    @PostMapping(value = "/uncollect")
    @ApiOperationLog(description = "笔记取消收藏")
    public Response<?> unCollectNote(@Validated @RequestBody UnCollectNoteReqVO unCollectNoteReqVO) {
        return noteService.unCollectNote(unCollectNoteReqVO);
    }

    //判断当前用户是否点赞、收藏
    @PostMapping(value = "/isLikedAndCollectedData")
    @ApiOperationLog(description = "获取当前用户是否点赞、收藏数据")
    public Response<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(@Validated @RequestBody FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO) {
        return noteService.isLikedAndCollectedData(findNoteIsLikedAndCollectedReqVO);
    }

}

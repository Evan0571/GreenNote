package com.evan.greennote.note.biz.service;

import com.evan.framework.common.response.Response;
import com.evan.greennote.note.biz.model.vo.*;

//笔记业务
public interface NoteService {
    //笔记发布
    Response<?> publishNote(PublishNoteReqVO publishNoteReqVO);
    //笔记详情
    Response<FindNoteDetailRspVO> findNoteDetail(FindNoteDetailReqVO findNoteDetailReqVO);
    //笔记更新
    Response<?> updateNote(UpdateNoteReqVO updateNoteReqVO);
    //笔记删除
    Response<?> deleteNote(DeleteNoteReqVO deleteNoteReqVO);
    //删除本地笔记缓存
    void deleteNoteLocalCache(Long noteId);
    //笔记仅对自己可见
    Response<?> visibleOnlyMe(UpdateNoteVisibleOnlyMeReqVO updateNoteVisibleOnlyMeReqVO);
    //笔记置顶/取消置顶
    Response<?> topNote(TopNoteReqVO topNoteReqVO);
    //笔记点赞
    Response<?> likeNote(LikeNoteReqVO likeNoteReqVO);
    //笔记取消点赞
    Response<?> unlikeNote(UnlikeNoteReqVO unlikeNoteReqVO);
    //笔记收藏
    Response<?> collectNote(CollectNoteReqVO collectNoteReqVO);
    //笔记取消收藏
    Response<?> unCollectNote(UnCollectNoteReqVO unCollectNoteReqVO);
    //获取是否点赞、收藏数据
    Response<FindNoteIsLikedAndCollectedRspVO> isLikedAndCollectedData(FindNoteIsLikedAndCollectedReqVO findNoteIsLikedAndCollectedReqVO);
    //用户主页 - 查询已发布的笔记列表
    Response<FindPublishedNoteListRspVO> findPublishedNoteList(FindPublishedNoteListReqVO findPublishedNoteListReqVO);
}

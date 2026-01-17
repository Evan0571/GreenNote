package com.evan.greennote.data.align.domain.mapper;

import org.apache.ibatis.annotations.Param;

public interface InsertMapper {
    //笔记点赞数：计划变更
    void insert2DataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("noteId") Long noteId);
    //用户获得点赞数：计划变更
    void insert2DataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
    //笔记收藏数：计划变更
    void insert2DataAlignNoteCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("noteId") Long noteId);
    //用户收藏数：计划变更
    void insert2DataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
    //笔记发布数：计划变更
    void insert2DataAlignUserNotePublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
    //用户关注数：计划变更
    void insert2DataAlignUserFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
    //用户粉丝数：计数变更
    void insert2DataAlignUserFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") Long userId);
}

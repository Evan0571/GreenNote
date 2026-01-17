package com.evan.greennote.data.align.domain.mapper;

public interface CreateTableMapper {
    //创建日增量表：关注数计数变更
    void createDataAlignFollowingCountTempTable(String tableNameSuffix);
    //创建日增量表：粉丝数计数变更
    void createDataAlignFansCountTempTable(String tableNameSuffix);
    //创建日增量表：笔记收藏数计数变更
    void createDataAlignNoteCollectCountTempTable(String tableNameSuffix);
    //创建日增量表：用户被收藏数计数变更
    void createDataAlignUserCollectCountTempTable(String tableNameSuffix);
    //创建日增量表：用户被点赞数计数变更
    void createDataAlignUserLikeCountTempTable(String tableNameSuffix);
    //创建日增量表：笔记点赞数计数变更
    void createDataAlignNoteLikeCountTempTable(String tableNameSuffix);
    //创建日增量表：笔记发布数计数变更
    void createDataAlignNotePublishCountTempTable(String tableNameSuffix);

    //清出旧表
    void dropDataAlignFollowingCountTempTable(String tableNameSuffix);

    void dropDataAlignFansCountTempTable(String tableNameSuffix);

    void dropDataAlignNoteCollectCountTempTable(String tableNameSuffix);

    void dropDataAlignUserCollectCountTempTable(String tableNameSuffix);

    void dropDataAlignUserLikeCountTempTable(String tableNameSuffix);

    void dropDataAlignNoteLikeCountTempTable(String tableNameSuffix);

    void dropDataAlignNotePublishCountTempTable(String tableNameSuffix);

}

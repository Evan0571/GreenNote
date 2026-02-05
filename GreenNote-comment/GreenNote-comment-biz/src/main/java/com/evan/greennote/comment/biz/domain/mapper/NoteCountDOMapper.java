package com.evan.greennote.comment.biz.domain.mapper;

import com.evan.greennote.comment.biz.domain.dataobject.NoteCountDO;
import org.apache.ibatis.annotations.Param;

public interface NoteCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(NoteCountDO record);

    int insertSelective(NoteCountDO record);

    NoteCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(NoteCountDO record);

    int updateByPrimaryKey(NoteCountDO record);

    //查询笔记评论总数
    Long selectCommentTotalByNoteId(Long noteId);

    //更新评论总数
    int updateCommentTotalByNoteId(@Param("noteId") Long noteId,
                                   @Param("count") int count);
}
package com.evan.greennote.note.biz.domain.mapper;

import com.evan.greennote.note.biz.domain.dataobject.TopicDO;

public interface TopicDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(TopicDO record);

    int insertSelective(TopicDO record);

    TopicDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TopicDO record);

    int updateByPrimaryKey(TopicDO record);

    String selectNameByPrimaryKey(Long id);
}
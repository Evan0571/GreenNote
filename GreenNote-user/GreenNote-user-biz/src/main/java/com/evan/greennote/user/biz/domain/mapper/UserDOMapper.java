package com.evan.greennote.user.biz.domain.mapper;

import com.evan.greennote.user.biz.domain.dataobject.UserDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserDO record);

    int insertSelective(UserDO record);

    UserDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserDO record);

    int updateByPrimaryKey(UserDO record);

    UserDO selectByEmail(String email);

    List<UserDO> selectByIds(@Param("ids") List<Long> ids);
}
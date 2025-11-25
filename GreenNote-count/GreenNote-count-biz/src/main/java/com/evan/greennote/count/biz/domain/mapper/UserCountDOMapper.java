package com.evan.greennote.count.biz.domain.mapper;

import com.evan.greennote.count.biz.domain.dataobject.UserCountDO;
import org.apache.ibatis.annotations.Param;

public interface UserCountDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(UserCountDO record);

    int insertSelective(UserCountDO record);

    UserCountDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserCountDO record);

    int updateByPrimaryKey(UserCountDO record);

    int insertOrUpdateFansTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateFollowingTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateLikeTotalByUserId(@Param("count") Integer count, @Param("userId") Long userId);

    int insertOrUpdateNoteTotalByUserId(@Param("count") Long count, @Param("userId") Long userId);
}
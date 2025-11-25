package com.evan.greennote.user.relation.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//查询粉丝列表
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindFansUserRspVO {
    private Long userId;
    private String avatar;
    private  String nickname;
    private Long fansTotal;
    private Long noteTotal;
}

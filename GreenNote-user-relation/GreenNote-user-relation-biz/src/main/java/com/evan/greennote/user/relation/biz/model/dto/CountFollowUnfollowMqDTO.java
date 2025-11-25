package com.evan.greennote.user.relation.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//通知计数服务：关注/取关
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountFollowUnfollowMqDTO {
    private Long userId;
    private Long targetUserId;
    private Integer type;//1-关注，0-取关
}

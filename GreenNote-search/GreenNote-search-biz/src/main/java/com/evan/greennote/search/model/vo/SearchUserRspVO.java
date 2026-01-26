package com.evan.greennote.search.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//搜索用户
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchUserRspVO {
    private Long userId;
    private String nickname;
    private String avatar;
    private String greennoteId;
    private Integer noteTotal;
    private String fansTotal;
    private String highlightNickname;
}
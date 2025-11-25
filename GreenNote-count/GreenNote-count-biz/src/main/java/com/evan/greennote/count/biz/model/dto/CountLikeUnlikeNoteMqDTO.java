package com.evan.greennote.count.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//点赞计数
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountLikeUnlikeNoteMqDTO {
    private Long userId;
    private Long noteId;
    private Integer type;
    private LocalDateTime createTime;
    private Long noteCreatorId;
}

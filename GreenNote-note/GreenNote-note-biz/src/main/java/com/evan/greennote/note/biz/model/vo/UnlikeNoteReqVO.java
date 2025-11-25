package com.evan.greennote.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//取消点赞
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnlikeNoteReqVO {
    @NotNull(message="笔记 ID 不能为空")
    private Long id;
}

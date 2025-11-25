package com.evan.greennote.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//笔记取消收藏
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UnCollectNoteReqVO {
    @NotNull(message="笔记 ID 不能为空")
    private Long id;
}

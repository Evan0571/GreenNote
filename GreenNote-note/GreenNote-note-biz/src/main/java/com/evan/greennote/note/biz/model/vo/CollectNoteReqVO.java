package com.evan.greennote.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//笔记收藏
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectNoteReqVO {
    @NotNull(message="笔记ID不能为空")
    private Long id;
}

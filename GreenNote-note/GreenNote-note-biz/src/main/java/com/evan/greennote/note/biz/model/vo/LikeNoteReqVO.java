package com.evan.greennote.note.biz.model.vo;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//笔记点赞
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikeNoteReqVO {
    @NotNull(message = "笔记ID不能为空")
    private Long id;
}

package com.evan.greennote.kv.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//查询笔记内容请求
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteContentReqDTO {
    @NotBlank(message ="笔记内容 UUID 不能为空")
    private String uuid;
}

package com.evan.greennote.data.align.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectUnCollectNoteMqDTO {
    private Long userId;
    private Long noteId;
    private Integer type;
    private Long noteCreatorId;
    private LocalDateTime createTime;
}

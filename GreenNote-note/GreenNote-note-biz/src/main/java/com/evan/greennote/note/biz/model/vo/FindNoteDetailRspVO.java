package com.evan.greennote.note.biz.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

//查询笔记详情
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindNoteDetailRspVO {
    private long id;
    private Integer type;
    private String title;
    private String content;
    private List<String> imgUris;
    private Long topicId;
    private String topicName;
    private Long creatorId;
    private String creatorName;
    private String avatar;
    private String videoUri;
    private LocalDateTime updateTime;
    private Integer visible;
}

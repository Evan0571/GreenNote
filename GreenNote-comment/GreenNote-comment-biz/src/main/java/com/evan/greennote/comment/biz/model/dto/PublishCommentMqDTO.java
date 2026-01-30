package com.evan.greennote.comment.biz.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//评论发布
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PublishCommentMqDTO {

    private Long noteId;

    private String content;

    private String imageUrl;

    private Long replyCommentId;

    private LocalDateTime createTime;

    private Long creatorId;

}
package com.evan.greennote.note.biz.convert;

import com.evan.greennote.note.biz.domain.dataobject.NoteDO;
import com.evan.greennote.note.biz.model.dto.PublishNoteDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

//实体类转换
@Mapper
public interface NoteConvert {

    //初始化 convert 实例
    NoteConvert INSTANCE = Mappers.getMapper(NoteConvert.class);

    //将 DO 转化为 DTO
    PublishNoteDTO convertDO2DTO(NoteDO bean);

    //将 DTO 转化为 DO
    NoteDO convertDTO2DO(PublishNoteDTO bean);
}

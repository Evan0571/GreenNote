package com.evan.greennote.kv.biz.service.impl;

import com.evan.framework.common.exception.BizException;
import com.evan.framework.common.response.Response;
import com.evan.greennote.kv.biz.domain.dataobject.NoteContentDO;
import com.evan.greennote.kv.biz.domain.repository.NoteContentRepository;
import com.evan.greennote.kv.biz.service.NoteContentService;
import com.evan.greennote.kv.dto.req.AddNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.DeleteNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.FindNoteContentReqDTO;
import com.evan.greennote.kv.dto.rsp.FindNoteContentRspDTO;
import com.evan.greennote.kv.enums.ResponseCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

//Key-Value 业务
@Service
@Slf4j
public class NoteContentServiceImpl implements NoteContentService {
    @Resource
    private NoteContentRepository noteContentRepository;

    //添加笔记内容
    @Override
    public Response<?> addNoteContent(AddNoteContentReqDTO addNoteContentReqDTO){
        //笔记内容 UUID
        String uuid = addNoteContentReqDTO.getUuid();
        //笔记内容
        String content = addNoteContentReqDTO.getContent();
        //创建数据库 DO 实体类
        NoteContentDO noteContentDO = NoteContentDO.builder()
                .id(UUID.fromString(uuid))
                .content(content)
                .build();
        //插入数据
        noteContentRepository.save(noteContentDO);
        return Response.success();
    }

    //查询笔记内容
    @Override
    public Response<FindNoteContentRspDTO> findNoteContent(FindNoteContentReqDTO findNoteContentReqDTO){
        //笔记ID
        String uuid=findNoteContentReqDTO.getUuid();
        //根据笔记 ID 查询笔记内容
        Optional<NoteContentDO> optional=noteContentRepository.findById(UUID.fromString(uuid));
        //若笔记内容不存在
        if(!optional.isPresent()){
            throw new BizException(ResponseCodeEnum.NOTE_CONTENT_NOT_FOUND);
        }
        NoteContentDO noteContentDO=optional.get();
        //构建反参
        FindNoteContentRspDTO findNoteContentRspDTO=FindNoteContentRspDTO.builder()
                .uuid(noteContentDO.getId())
                .content(noteContentDO.getContent())
                .build();
        return Response.success(findNoteContentRspDTO);
    }

    //删除笔记内容

    @Override
    public Response<?> deleteNoteContent(DeleteNoteContentReqDTO deleteNoteContentReqDTO) {
        //笔记ID
        String uuid=deleteNoteContentReqDTO.getUuid();
        //删除笔记内容
        noteContentRepository.deleteById(UUID.fromString(uuid));
        return Response.success();
    }
}

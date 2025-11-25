package com.evan.greennote.note.biz.rpc;

import com.evan.framework.common.response.Response;
import com.evan.greennote.kv.api.KeyValueFeignApi;
import com.evan.greennote.kv.dto.req.AddNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.DeleteNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.FindNoteContentReqDTO;
import com.evan.greennote.kv.dto.rsp.FindNoteContentRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Objects;

//KV键值服务
@Component
public class KeyValueRpcService {
    @Resource
    private KeyValueFeignApi keyValueFeignApi;

    //保存笔记内容
    public boolean saveNoteContent(String uuid, String content){
        AddNoteContentReqDTO addNoteContentReqDTO=new AddNoteContentReqDTO();
        addNoteContentReqDTO.setUuid(uuid);
        addNoteContentReqDTO.setContent(content);

        Response<?> response=keyValueFeignApi.addNoteContent(addNoteContentReqDTO);

        if(Objects.isNull(response)||!response.isSuccess()){
            return false;
        }
        return true;
    }

    //删除笔记内容
    public boolean deleteNoteContent(String uuid){
        DeleteNoteContentReqDTO deleteNoteContentReqDTO=new DeleteNoteContentReqDTO();
        deleteNoteContentReqDTO.setUuid(uuid);

        Response<?> response=keyValueFeignApi.deleteNoteContent(deleteNoteContentReqDTO);

        if(Objects.isNull(response)||!response.isSuccess()){
            return false;
        }
        return true;
    }

    //查找笔记内容
    public String findNoteContent(String uuid){
        FindNoteContentReqDTO findNoteContentReqDTO=new FindNoteContentReqDTO();
        findNoteContentReqDTO.setUuid(uuid);
        Response<FindNoteContentRspDTO> response=keyValueFeignApi.findNoteContent(findNoteContentReqDTO);
        if(Objects.isNull(response)||!response.isSuccess()||Objects.isNull(response.getData())){
            return null;
        }
        return response.getData().getContent();
    }
}

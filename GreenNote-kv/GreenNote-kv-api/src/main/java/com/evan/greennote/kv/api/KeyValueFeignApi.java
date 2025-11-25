package com.evan.greennote.kv.api;

import com.evan.framework.common.response.Response;
import com.evan.greennote.kv.constant.ApiConstants;
import com.evan.greennote.kv.dto.req.AddNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.DeleteNoteContentReqDTO;
import com.evan.greennote.kv.dto.req.FindNoteContentReqDTO;
import com.evan.greennote.kv.dto.rsp.FindNoteContentRspDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

//K-V 键值存储 Feign 接口
@FeignClient(name= ApiConstants.SERVICE_NAME)
public interface KeyValueFeignApi {
    String PREFIX="/kv";

    //添加笔记内容
    @PostMapping(value=PREFIX+"/note/content/add")
    Response<?> addNoteContent(@RequestBody AddNoteContentReqDTO addNoteContentReqDTO);

    //查询笔记内容
    @PostMapping(value=PREFIX+"/note/content/find")
    Response<FindNoteContentRspDTO> findNoteContent(@RequestBody FindNoteContentReqDTO findNoteContentReqDTO);

    //删除笔记内容
    @PostMapping(value=PREFIX+"/note/content/delete")
    Response<?> deleteNoteContent(@RequestBody DeleteNoteContentReqDTO deleteNoteContentReqDTO);
}

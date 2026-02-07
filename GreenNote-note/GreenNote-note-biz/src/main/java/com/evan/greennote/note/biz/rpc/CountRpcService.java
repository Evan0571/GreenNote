package com.evan.greennote.note.biz.rpc;

import cn.hutool.core.collection.CollUtil;
import com.evan.framework.common.response.Response;
import com.evan.greennote.count.api.CountFeignApi;
import com.evan.greennote.count.dto.FindNoteCountsByIdReqDTO;
import com.evan.greennote.count.dto.FindNoteCountsByIdRspDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

//计数服务
@Component
public class CountRpcService {

    @Resource
    private CountFeignApi countFeignApi;

    //批量查询笔记计数
    public List<FindNoteCountsByIdRspDTO> findByNoteIds(List<Long> noteIds) {
        FindNoteCountsByIdReqDTO findNoteCountsByIdReqDTO = new FindNoteCountsByIdReqDTO();
        findNoteCountsByIdReqDTO.setNoteIds(noteIds);

        Response<List<FindNoteCountsByIdRspDTO>> response = countFeignApi.findNotesCount(findNoteCountsByIdReqDTO);

        if (!response.isSuccess() || Objects.isNull(response.getData()) || CollUtil.isEmpty(response.getData())) {
            return null;
        }

        return response.getData();
    }

}
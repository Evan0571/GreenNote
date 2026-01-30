package com.evan.greennote.data.align.rpc;

import com.evan.greennote.search.api.SearchFeignApi;
import com.evan.greennote.search.dto.RebuildNoteDocumentReqDTO;
import com.evan.greennote.search.dto.RebuildUserDocumentReqDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

//搜索服务
@Component
public class SearchRpcService {

    @Resource
    private SearchFeignApi searchFeignApi;

    //调用重建笔记文档接口
    public void rebuildNoteDocument(Long noteId) {
        RebuildNoteDocumentReqDTO rebuildNoteDocumentReqDTO = RebuildNoteDocumentReqDTO.builder()
                .id(noteId)
                .build();

        searchFeignApi.rebuildNoteDocument(rebuildNoteDocumentReqDTO);
    }

    //调用重建用户文档接口
    public void rebuildUserDocument(Long userId) {
        RebuildUserDocumentReqDTO rebuildUserDocumentReqDTO = RebuildUserDocumentReqDTO.builder()
                .id(userId)
                .build();

        searchFeignApi.rebuildUserDocument(rebuildUserDocumentReqDTO);
    }

}
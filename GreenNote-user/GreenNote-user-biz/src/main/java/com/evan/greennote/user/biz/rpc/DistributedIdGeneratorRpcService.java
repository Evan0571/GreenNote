package com.evan.greennote.user.biz.rpc;

import com.evan.greennote.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

//分布式ID生成服务
@Component
public class DistributedIdGeneratorRpcService {
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;
    //Leaf号段模式：greennote ID 业务标识
    private static final String BIZ_TAG_GREENNOTE_ID="leaf-segment-greennote-id";
    private static final String BIZ_TAG_USER_ID = "leaf-segment-user-id";
    //调用分布式 ID 生成服务生成 greennote ID
    public String getGreenNoteId(){
        return distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_GREENNOTE_ID);
    }
    public Long getUserId() {
        return Long.valueOf(distributedIdGeneratorFeignApi.getSegmentId(BIZ_TAG_USER_ID));
    }
}

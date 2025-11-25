package com.evan.greennote.note.biz.rpc;

import com.evan.greennote.distributed.id.generator.api.DistributedIdGeneratorFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

//用户服务
@Component
public class DistributedIdGeneratorRpcService {
    @Resource
    private DistributedIdGeneratorFeignApi distributedIdGeneratorFeignApi;
    //生成雪花算法ID
    public String getSnowflakeId(){
        return distributedIdGeneratorFeignApi.getSnowflakeId("test");
    }
}

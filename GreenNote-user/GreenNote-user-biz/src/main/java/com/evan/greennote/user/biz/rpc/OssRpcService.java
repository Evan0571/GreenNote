package com.evan.greennote.user.biz.rpc;

import com.evan.framework.common.response.Response;
import com.evan.greennote.oss.api.FileFeignApi;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

//对象存储服务调用
@Component
public class OssRpcService {
    @Resource
    private FileFeignApi fileFeignApi;

    public String uploadFile(MultipartFile file){
        //调用对象存储服务上传文件
        Response<?> response=fileFeignApi.uploadFile(file);
        if(!response.isSuccess()){
            return null;
        }
        //返回图片链接
        return (String) response.getData();
    }
}

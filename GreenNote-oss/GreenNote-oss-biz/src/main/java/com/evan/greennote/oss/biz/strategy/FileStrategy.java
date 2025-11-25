package com.evan.greennote.oss.biz.strategy;

import org.springframework.web.multipart.MultipartFile;

//文件策略接口
public interface FileStrategy {
    //文件上传
    String uploadFile(MultipartFile file, String bucketName);
}

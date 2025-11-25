package com.evan.greennote.oss.biz.service;

import com.evan.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    //上传文件
    Response<?> uploadFile(MultipartFile file);
}

package com.evan.greennote.search.biz.service;

import com.evan.framework.common.response.Response;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    //上传文件
    Response<?> uploadFile(MultipartFile file);
}

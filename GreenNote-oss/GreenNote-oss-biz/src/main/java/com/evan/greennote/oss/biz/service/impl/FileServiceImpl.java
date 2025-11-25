package com.evan.greennote.oss.biz.service.impl;

import com.evan.framework.common.response.Response;
import com.evan.greennote.oss.biz.service.FileService;
import com.evan.greennote.oss.biz.strategy.FileStrategy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Resource
    private FileStrategy fileStrategy;

    private static final String BUCKET_NAME = "greennote";

    @Override
    public Response<?> uploadFile(MultipartFile file) {
        // 上传文件到 greennote bucket
        String url=fileStrategy.uploadFile(file, BUCKET_NAME);
        return Response.success(url);
    }
}

package com.evan.greennote.oss.biz.strategy.impl;

import com.evan.greennote.oss.biz.config.MinioProperties;
import com.evan.greennote.oss.biz.strategy.FileStrategy;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
public class MinioFileStrategy implements FileStrategy {

    @Resource
    private MinioProperties minioProperties;
    @Resource
    private MinioClient minioClient;

    @Override
    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName){
        log.info("## 上传文件至 Minio ...");

        //判断文件是否为空
        if(file==null||file.getSize()==0){
            log.info("==> 上传文件异常：文件大小为空 ...");
            throw new RuntimeException("文件大小不能为空");
        }

        //文件原始名称
        String originalFilename = file.getOriginalFilename();
        //文件的 Content-Type
        String contentType = file.getContentType();
        //生成储存对象名称（替换字符串中的“-”为空字符串）
        String key= UUID.randomUUID().toString().replace("-", "");
        //获取文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        //拼接名字与后缀
        String objectName=String.format("%s%s",key, suffix);

        log.info("==> 开始上传文件至 Minio，ObjectName：{}，objectName");

        //上传文件至 Minio
        minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(contentType)
                        .build());

        //返回文件访问链接
        String url=String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectName);
        log.info("==> 上传文件至 Minio 成功，访问路径：{}",url);
        return url;
    }
}

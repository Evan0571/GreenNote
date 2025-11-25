package com.evan.greennote.oss.api;

import com.evan.framework.common.response.Response;
import com.evan.greennote.oss.constant.ApiConstants;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name= ApiConstants.SERVICE_NAME)
public interface FileFeignApi {
    String PREFIX="/file";

    @PostMapping(value=PREFIX+"/upload",consumes= MediaType.MULTIPART_FORM_DATA_VALUE)
    Response<?> uploadFile(@RequestPart(value="file") MultipartFile file);
}

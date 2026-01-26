package com.evan.greennote.search.controller;

import com.evan.framework.biz.operationlog.aspect.ApiOperationLog;
import com.evan.greennote.search.service.ExtStopWordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//热更新停用词典
@RestController
@RequestMapping("/search")
@Slf4j
public class ExtStopWordController {

    @Resource
    private ExtStopWordService extStopWordService;

    @GetMapping("/ext/stop/word")
    @ApiOperationLog(description = "热更新停用词典")
    public ResponseEntity<String> extStopWord() {
        return extStopWordService.getStopWordHotUpdateExtDict();
    }

}
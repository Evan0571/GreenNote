package com.evan.greennote.search.service;

import org.springframework.http.ResponseEntity;

//拓展停用词词典
public interface ExtStopWordService {
    //获取热更新停用词词典
    ResponseEntity<String> getStopWordHotUpdateExtDict();
}

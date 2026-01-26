package com.evan.greennote.search.service;

import org.springframework.http.ResponseEntity;

//拓展词典
public interface ExtDictService {
    //获取热更新词典
    ResponseEntity<String> getHotUpdateExtDict();
}

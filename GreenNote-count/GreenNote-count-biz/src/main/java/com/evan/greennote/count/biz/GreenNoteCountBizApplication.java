package com.evan.greennote.count.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.evan.greennote.count.biz.domain.mapper")
public class GreenNoteCountBizApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GreenNoteCountBizApplication.class, args);
    }
}

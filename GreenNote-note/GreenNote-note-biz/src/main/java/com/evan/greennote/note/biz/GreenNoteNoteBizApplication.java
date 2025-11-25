package com.evan.greennote.note.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.evan.greennote.note.biz.domain.mapper")
@EnableFeignClients(basePackages="com.evan.greennote")
public class GreenNoteNoteBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenNoteNoteBizApplication.class, args);
    }
}

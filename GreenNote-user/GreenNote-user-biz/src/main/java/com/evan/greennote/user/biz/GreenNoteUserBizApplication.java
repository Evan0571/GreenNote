package com.evan.greennote.user.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan("com.evan.greennote.user.biz.domain.mapper")
@EnableFeignClients(basePackages="com.evan.greennote")
public class GreenNoteUserBizApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenNoteUserBizApplication.class, args);
    }
}

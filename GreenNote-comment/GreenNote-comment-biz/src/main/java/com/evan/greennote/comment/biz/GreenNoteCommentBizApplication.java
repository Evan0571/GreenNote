package com.evan.greennote.comment.biz;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@MapperScan("com.evan.greennote.comment.biz.domain.mapper")
@EnableRetry
public class GreenNoteCommentBizApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenNoteCommentBizApplication.class, args);
    }

}

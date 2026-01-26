package com.evan.greennote.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.evan.greennote.search.domain.mapper")
public class GreenNoteSearchApplication {
    public static void main(String[] args) {
        SpringApplication.run(GreenNoteSearchApplication.class, args);
    }
}

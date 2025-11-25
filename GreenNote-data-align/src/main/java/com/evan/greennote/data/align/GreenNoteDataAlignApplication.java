package com.evan.greennote.data.align;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.evan.greennote.data.align.domain.mapper")
public class GreenNoteDataAlignApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GreenNoteDataAlignApplication.class, args);
    }
}

package com.evan.greennote.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

//密码加密
@Component
public class PasswordEncoderConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        //BCrypt哈希算法储存密码
        return new BCryptPasswordEncoder();
    }
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("Evan828828"));
    }
}

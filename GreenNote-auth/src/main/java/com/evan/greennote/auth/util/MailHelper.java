package com.evan.greennote.auth.util;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailHelper {

    @Resource
    private JavaMailSender mailSender;

    @Value("${mail.from}")
    private String from;

    public boolean sendText(String to, String subject, String content) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(content);
            mailSender.send(msg);
            log.info("==> 邮件发送成功, to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("==> 邮件发送失败, to: {}", to, e);
            return false;
        }
    }
}
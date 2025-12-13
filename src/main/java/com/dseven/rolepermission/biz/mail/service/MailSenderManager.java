package com.dseven.rolepermission.biz.mail.service;

import com.dseven.rolepermission.biz.mail.config.EmailProperties;
import com.dseven.rolepermission.biz.mail.enums.EmailBizType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailSenderManager {

    private final JavaMailSender javaMailSender;
    private final EmailProperties emailProperties;

    // 配置线程池，避免占用主线程池
    @Async("mailExecutor")
    public void sendAsync(String email, EmailBizType type, String code) {
        try {
            log.info("开始发送邮件: email={}, type={}", email, type);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailProperties.getFromAddress()); // 从配置读取发件人地址
            message.setTo(email);
            message.setSubject("【RolePermission】" + type.getDesc());
            message.setText("您的验证码是：" + code + "，有效期10分钟。请勿泄露给他人。");
            
            javaMailSender.send(message);
            log.info("邮件发送成功: email={}", email);
        } catch (Exception e) {
            log.error("邮件发送失败: {} -> {}", email, e.getMessage());
            // 可以在这里做简单的重试或记录失败日志表供定时任务重发
        }
    }
}

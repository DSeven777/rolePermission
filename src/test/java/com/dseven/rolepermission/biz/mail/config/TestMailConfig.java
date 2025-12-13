package com.dseven.rolepermission.biz.mail.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 测试配置 - 创建 Mock 的 JavaMailSender
 */
@TestConfiguration
public class TestMailConfig {

    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }
}
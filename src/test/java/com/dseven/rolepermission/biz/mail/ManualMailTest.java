package com.dseven.rolepermission.biz.mail;

import com.dseven.rolepermission.biz.mail.config.EmailProperties;
import com.dseven.rolepermission.biz.mail.enums.EmailBizType;
import com.dseven.rolepermission.biz.mail.config.TestMailConfig;
import com.dseven.rolepermission.biz.mail.service.MailSenderManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Scanner;

/**
 * 手动邮件发送测试工具
 * 
 * 这是一个独立的主程序，用于验证真实邮件发送功能。
 * 它会加载完整的 Spring 上下文，读取 application-dev.yml 配置。
 */
@SpringBootApplication
@ComponentScan(
    basePackages = "com.dseven.rolepermission",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE, 
        classes = TestMailConfig.class
    )
)
public class ManualMailTest {

    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "dev"); // 强制使用 dev 配置
        
        try (ConfigurableApplicationContext context = SpringApplication.run(ManualMailTest.class, args)) {
            System.out.println("\n============================================");
            System.out.println("🔥 邮件测试工具已启动");
            System.out.println("============================================\n");

            MailSenderManager mailSenderManager = context.getBean(MailSenderManager.class);
            EmailProperties emailProperties = context.getBean(EmailProperties.class);
            JavaMailSender javaMailSender = context.getBean(JavaMailSender.class);

            System.out.println("JavaMailSender 类型: " + javaMailSender.getClass().getName());
            if (javaMailSender.getClass().getName().contains("Mockito")) {
                System.err.println("❌ 警告：检测到 Mock 对象，无法发送真实邮件！请检查 TestMailConfig 是否被排除。");
                return;
            }

            System.out.println("当前配置发件人: " + emailProperties.getFromAddress());
            System.out.println("当前配置Host: " + context.getEnvironment().getProperty("spring.mail.host"));
            
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n请输入接收验证码的邮箱地址: ");
            String toEmail = scanner.nextLine().trim();

            if (toEmail.isEmpty()) {
                System.out.println("❌ 邮箱不能为空");
                return;
            }

            System.out.println("正在发送邮件给 " + toEmail + " ...");
            
            // 发送测试邮件
            mailSenderManager.sendAsync(toEmail, EmailBizType.REGISTER, "888888");
            
            // 由于 sendAsync 是异步的，主线程需要等待一下，否则应用关闭线程池也就关了
            System.out.println("⏳ 邮件已提交到线程池，等待发送完成...");
            Thread.sleep(10000); 
            
            System.out.println("\n✅ 测试结束，请检查收件箱（包括垃圾箱）");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

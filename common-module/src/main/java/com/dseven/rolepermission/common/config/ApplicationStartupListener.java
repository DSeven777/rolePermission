package com.dseven.rolepermission.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 应用启动监听器
 */
@Slf4j
@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();

        try {
            String ip = InetAddress.getLocalHost().getHostAddress();
            String port = env.getProperty("server.port", "8080");
            String contextPath = env.getProperty("server.servlet.context-path", "");
            String[] activeProfiles = env.getActiveProfiles();
            String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

            // 拼接访问地址
            String ipPort = "http://" + ip + ":" + port + contextPath;
            String externalAccess = "http://localhost:" + port + contextPath;

            // 构建分隔线
            String separator = "──────────────────────────────────────────────────────────────────";

            // 打印启动信息
            log.info("\n" + separator);
            log.info("🚀 角色权限管理系统 (Role Permission System) 启动成功！");
            log.info(separator);
            log.info("📍 本地访问地址: {}", externalAccess);
            log.info("📍 外部访问地址: {}", ipPort);
            log.info("📍 API 文档 (Swagger): {}swagger-ui.html", externalAccess);
            log.info("📍 API 文档 (OpenAPI): {}v3/api-docs", externalAccess);
            log.info("📍 健康检查地址: {}actuator/health", externalAccess);
            log.info(separator);
            log.info("🔧 当前环境: {}", profile);
            log.info("🌐 服务IP: {}", ip);
            log.info("🔌 服务端口: {}", port);
            log.info("📂 上下文路径: {}", contextPath.isEmpty() ? "/" : contextPath);
            log.info("⚡ Java版本: {}", System.getProperty("java.version"));
            log.info("⚡ Spring Boot版本: {}", env.getProperty("spring.boot.version"));
            log.info(separator);
            log.info("🎉 系统已就绪，可以开始使用！");
            log.info(separator + "\n");

        } catch (UnknownHostException e) {
            log.error("无法获取本地IP地址", e);
        }
    }
}
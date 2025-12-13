package com.dseven.rolepermission;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class RolePermissionApplication {

    public static void main(String[] args) {
        // 打印启动信息
        log.info("🔥 正在启动角色权限管理系统...");
        log.info("⏳ 系统初始化中，请稍候...");

        SpringApplication app = new SpringApplication(RolePermissionApplication.class);
        Environment env = app.run(args).getEnvironment();

        String[] activeProfiles = env.getActiveProfiles();
        String profile = activeProfiles.length > 0 ? activeProfiles[0] : "default";

        log.info("✅ 系统启动成功！当前环境: {}", profile);
    }
}

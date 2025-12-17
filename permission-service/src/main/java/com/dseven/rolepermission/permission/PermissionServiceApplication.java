package com.dseven.rolepermission.permission;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.dseven.rolepermission.permission", "com.dseven.rolepermission.common"})
@MapperScan("com.dseven.rolepermission.permission.mapper")
public class PermissionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PermissionServiceApplication.class, args);
    }
}

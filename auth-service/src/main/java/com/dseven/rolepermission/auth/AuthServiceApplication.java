package com.dseven.rolepermission.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.dseven.rolepermission.common.feign")
@ComponentScan(basePackages = {"com.dseven.rolepermission.auth", "com.dseven.rolepermission.common"})
@MapperScan("com.dseven.rolepermission.auth.mapper") // If any, mostly uses Feign
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
